-- Farm2Market Supabase schema. Run once in the SQL editor of a new project.
create schema if not exists extensions;
create extension if not exists cube with schema extensions;
create extension if not exists earthdistance with schema extensions;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  role text not null check (role in ('customer', 'farmer')),
  display_name text not null,
  latitude double precision not null check (latitude between -90 and 90),
  longitude double precision not null check (longitude between -180 and 180),
  created_at timestamptz not null default now()
);

create table if not exists public.products (
  id uuid primary key default gen_random_uuid(),
  seller_id uuid not null references public.profiles(id) on delete cascade,
  name text not null check (length(trim(name)) between 1 and 80),
  category text not null check (category in ('vegetables', 'fruits', 'greens', 'grains')),
  price numeric(10, 2) not null check (price > 0),
  emoji text not null default '🌱',
  stock_quantity integer not null default 0 check (stock_quantity >= 0),
  is_listed boolean not null default true,
  updated_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create table if not exists public.orders (
  id uuid primary key default gen_random_uuid(),
  buyer_id uuid not null references public.profiles(id),
  seller_id uuid not null references public.profiles(id),
  buyer_name text not null,
  status text not null default 'pending'
    check (status in ('pending', 'accepted', 'ready', 'out_for_delivery', 'delivered', 'cancelled')),
  total numeric(12, 2) not null default 0 check (total >= 0),
  buyer_latitude double precision not null,
  buyer_longitude double precision not null,
  created_at timestamptz not null default now()
);

create table if not exists public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  product_id uuid not null references public.products(id),
  product_name text not null,
  quantity integer not null check (quantity > 0),
  unit_price numeric(10, 2) not null check (unit_price > 0)
);

create index if not exists products_seller_id_idx on public.products(seller_id);
create index if not exists products_market_idx on public.products(is_listed, stock_quantity);
create index if not exists orders_buyer_created_idx on public.orders(buyer_id, created_at desc);
create index if not exists orders_seller_created_idx on public.orders(seller_id, created_at desc);
create index if not exists order_items_order_id_idx on public.order_items(order_id);

alter table public.profiles enable row level security;
alter table public.products enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;

drop policy if exists "read own profile" on public.profiles;
create policy "read own profile" on public.profiles
  for select to authenticated using (id = auth.uid());
drop policy if exists "update own location" on public.profiles;
create policy "update own location" on public.profiles
  for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists "read listed products" on public.products;
create policy "read listed products" on public.products
  for select to authenticated using (is_listed or seller_id = auth.uid());
drop policy if exists "farmers insert own products" on public.products;
create policy "farmers insert own products" on public.products
  for insert to authenticated with check (
    seller_id = auth.uid() and exists (
      select 1 from public.profiles p where p.id = auth.uid() and p.role = 'farmer'
    )
  );
drop policy if exists "farmers update own products" on public.products;
create policy "farmers update own products" on public.products
  for update to authenticated using (seller_id = auth.uid())
  with check (seller_id = auth.uid());
drop policy if exists "farmers delete own products" on public.products;
create policy "farmers delete own products" on public.products
  for delete to authenticated using (seller_id = auth.uid());

drop policy if exists "participants read orders" on public.orders;
create policy "participants read orders" on public.orders
  for select to authenticated using (buyer_id = auth.uid() or seller_id = auth.uid());
drop policy if exists "sellers update order status" on public.orders;
create policy "sellers update order status" on public.orders
  for update to authenticated using (seller_id = auth.uid())
  with check (seller_id = auth.uid());
drop policy if exists "participants read order items" on public.order_items;
create policy "participants read order items" on public.order_items
  for select to authenticated using (
    exists (select 1 from public.orders o where o.id = order_id and
      (o.buyer_id = auth.uid() or o.seller_id = auth.uid()))
  );

revoke all on public.profiles, public.products, public.orders, public.order_items from anon, authenticated;
grant select on public.profiles to authenticated;
grant update (latitude, longitude) on public.profiles to authenticated;
grant select, insert, update, delete on public.products to authenticated;
grant select on public.orders to authenticated;
grant update (status) on public.orders to authenticated;
grant select on public.order_items to authenticated;

create or replace function public.ensure_profile(
  p_role text, p_display_name text, p_latitude double precision, p_longitude double precision
)
returns public.profiles language plpgsql security definer set search_path = pg_catalog
as $$
declare v_profile public.profiles;
begin
  if auth.uid() is null then raise exception 'Sign in before creating a profile.'; end if;
  if p_role not in ('customer', 'farmer') then raise exception 'Invalid account type.'; end if;
  if p_latitude not between -90 and 90 or p_longitude not between -180 and 180 then
    raise exception 'A valid device location is required.';
  end if;
  select * into v_profile from public.profiles where id = auth.uid() for update;
  if found then
    if v_profile.role <> p_role then raise exception 'role does not match this app'; end if;
    update public.profiles set latitude = p_latitude, longitude = p_longitude,
      display_name = case when length(trim(p_display_name)) > 0 then trim(p_display_name) else display_name end
      where id = auth.uid() returning * into v_profile;
    return v_profile;
  end if;
  if length(trim(coalesce(p_display_name, ''))) = 0 then
    raise exception 'Enter your name to finish creating the account.';
  end if;
  insert into public.profiles(id, role, display_name, latitude, longitude)
    values (auth.uid(), p_role, trim(p_display_name), p_latitude, p_longitude)
    returning * into v_profile;
  return v_profile;
end;
$$;
revoke all on function public.ensure_profile(text, text, double precision, double precision) from public, anon;
grant execute on function public.ensure_profile(text, text, double precision, double precision) to authenticated;

create or replace function public.touch_product_updated_at()
returns trigger language plpgsql set search_path = pg_catalog
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;
drop trigger if exists touch_product_updated_at on public.products;
create trigger touch_product_updated_at before update on public.products
  for each row execute function public.touch_product_updated_at();

create or replace function public.refresh_products_after_seller_move()
returns trigger language plpgsql security definer set search_path = pg_catalog
as $$
begin
  if new.latitude is distinct from old.latitude or new.longitude is distinct from old.longitude then
    update public.products set updated_at = now() where seller_id = new.id;
  end if;
  return new;
end;
$$;
drop trigger if exists refresh_products_after_seller_move on public.profiles;
create trigger refresh_products_after_seller_move after update of latitude, longitude on public.profiles
  for each row execute function public.refresh_products_after_seller_move();

create or replace function public.nearby_products(
  p_latitude double precision,
  p_longitude double precision,
  p_radius_m double precision default 20000
)
returns table (
  product_id uuid, seller_id uuid, name text, category text, price numeric,
  emoji text, stock_quantity integer, seller_name text, distance_m double precision
)
language plpgsql stable security definer set search_path = pg_catalog, extensions
as $$
begin
  if auth.uid() is null then raise exception 'Sign in to browse nearby produce.'; end if;
  if not exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'customer') then
    raise exception 'Only customer accounts can browse the marketplace.';
  end if;
  if p_radius_m <= 0 or p_radius_m > 20000 then raise exception 'Search radius cannot exceed 20 km.'; end if;
  return query
    select pr.id, pr.seller_id, pr.name, pr.category, pr.price, pr.emoji,
      pr.stock_quantity, seller.display_name,
      earth_distance(ll_to_earth(p_latitude, p_longitude),
        ll_to_earth(seller.latitude, seller.longitude)) as distance_m
    from public.products pr
    join public.profiles seller on seller.id = pr.seller_id
    where pr.is_listed and earth_distance(
      ll_to_earth(p_latitude, p_longitude),
      ll_to_earth(seller.latitude, seller.longitude)
    ) <= p_radius_m
    order by distance_m, pr.created_at desc;
end;
$$;
revoke all on function public.nearby_products(double precision, double precision, double precision) from public, anon;
grant execute on function public.nearby_products(double precision, double precision, double precision) to authenticated;

create or replace function public.place_orders(
  p_items jsonb, p_latitude double precision, p_longitude double precision
)
returns jsonb language plpgsql security definer set search_path = pg_catalog, extensions
as $$
declare
  v_buyer public.profiles;
  v_seller record;
  v_item record;
  v_product public.products;
  v_order_id uuid;
  v_total numeric(12, 2);
  v_result jsonb := '[]'::jsonb;
begin
  if auth.uid() is null then raise exception 'Sign in before placing an order.'; end if;
  select * into v_buyer from public.profiles where id = auth.uid() for update;
  if not found or v_buyer.role <> 'customer' then raise exception 'A customer profile is required.'; end if;
  if abs(v_buyer.latitude - p_latitude) > 0.02 or abs(v_buyer.longitude - p_longitude) > 0.02 then
    raise exception 'Refresh your location and try again.';
  end if;
  if p_items is null or jsonb_typeof(p_items) <> 'array' or jsonb_array_length(p_items) = 0 then
    raise exception 'Your cart is empty.';
  end if;
  if exists (
    select 1 from jsonb_to_recordset(p_items) as i(product_id uuid, quantity integer)
      left join public.products p on p.id = i.product_id
    where i.quantity is null or i.quantity <= 0 or p.id is null or not p.is_listed
  ) then raise exception 'A cart item is invalid or no longer available.'; end if;

  -- Each seller gets a separate order. This function runs as one transaction,
  -- so a stale or insufficient cart rolls back every stock reservation.
  for v_seller in
    select distinct p.seller_id, seller.latitude, seller.longitude
      from jsonb_to_recordset(p_items) as i(product_id uuid, quantity integer)
      join public.products p on p.id = i.product_id
      join public.profiles seller on seller.id = p.seller_id
  loop
    if earth_distance(
      ll_to_earth(v_buyer.latitude, v_buyer.longitude),
      ll_to_earth(v_seller.latitude, v_seller.longitude)
    ) > 20000 then raise exception 'A farmer in your cart is outside the 20 km delivery area.'; end if;

    insert into public.orders(buyer_id, seller_id, buyer_name, status, total, buyer_latitude, buyer_longitude)
      values (auth.uid(), v_seller.seller_id, v_buyer.display_name, 'pending', 0,
        v_buyer.latitude, v_buyer.longitude)
      returning id into v_order_id;
    v_total := 0;

    for v_item in
      select i.product_id, sum(i.quantity)::integer as quantity
        from jsonb_to_recordset(p_items) as i(product_id uuid, quantity integer)
        join public.products p on p.id = i.product_id
        where p.seller_id = v_seller.seller_id
        group by i.product_id
    loop
      if v_item.quantity <= 0 then raise exception 'Order quantities must be positive.'; end if;
      update public.products set stock_quantity = stock_quantity - v_item.quantity
        where id = v_item.product_id and seller_id = v_seller.seller_id
          and is_listed and stock_quantity >= v_item.quantity
        returning * into v_product;
      if not found then raise exception 'Stock changed while you were checking out. Refresh your cart.'; end if;
      insert into public.order_items(order_id, product_id, product_name, quantity, unit_price)
        values (v_order_id, v_product.id, v_product.name, v_item.quantity, v_product.price);
      v_total := v_total + (v_product.price * v_item.quantity);
    end loop;
    update public.orders set total = v_total where id = v_order_id;
    v_result := v_result || jsonb_build_array(v_order_id::text);
  end loop;
  return v_result;
end;
$$;
revoke all on function public.place_orders(jsonb, double precision, double precision) from public, anon;
grant execute on function public.place_orders(jsonb, double precision, double precision) to authenticated;

create or replace function public.check_order_status_transition()
returns trigger language plpgsql set search_path = pg_catalog
as $$
begin
  if new.status = old.status then return new; end if;
  if not (
    (old.status = 'pending' and new.status in ('accepted', 'cancelled')) or
    (old.status = 'accepted' and new.status = 'ready') or
    (old.status = 'ready' and new.status = 'out_for_delivery') or
    (old.status = 'out_for_delivery' and new.status = 'delivered')
  ) then raise exception 'That order status change is not allowed.'; end if;
  return new;
end;
$$;
drop trigger if exists validate_order_status_transition on public.orders;
create trigger validate_order_status_transition before update of status on public.orders
  for each row execute function public.check_order_status_transition();

-- Realtime clients reload rows after product and order changes.
alter table public.products replica identity full;
alter table public.orders replica identity full;
do $$
begin
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'products') then
    alter publication supabase_realtime add table public.products;
  end if;
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'orders') then
    alter publication supabase_realtime add table public.orders;
  end if;
end;
$$;
