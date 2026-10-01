package com.farm2market.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

data class AppLanguage(val code: String, val shortCode: String, val nativeName: String)

val appLanguages = listOf(
    AppLanguage("en", "EN", "English"),
    AppLanguage("te", "TE", "తెలుగు"),
    AppLanguage("hi", "HI", "हिन्दी"),
    AppLanguage("kn", "KN", "ಕನ್ನಡ"),
    AppLanguage("ml", "ML", "മലയാളം"),
    AppLanguage("ta", "TA", "தமிழ்")
)

val LocalAppLanguage = compositionLocalOf { "en" }

@Composable
fun ProvideAppLanguage(language: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLanguage provides language, content = content)
}

@Composable
fun LanguagePicker(
    language: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = appLanguages.firstOrNull { it.code == language } ?: appLanguages.first()
    androidx.compose.foundation.layout.Box(modifier) {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Choose language")
            MaterialText(selected.shortCode)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            appLanguages.forEach { option ->
                DropdownMenuItem(
                    text = { MaterialText(option.nativeName) },
                    onClick = {
                        expanded = false
                        onLanguageChange(option.code)
                    }
                )
            }
        }
    }
}

/** All shared Material text passes through this function so a language change updates every screen. */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    MaterialText(
        text = localizeAppText(text, LocalAppLanguage.current),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

private val translationColumns = listOf("te", "hi", "kn", "ml", "ta")

private val translations: Map<String, Map<String, String>> = run {
    val result = mutableMapOf<String, MutableMap<String, String>>()
    val rows = """
        Live¦ప్రత్యక్షం¦लाइव¦ನೇರ¦തത്സമയം¦நேரலை
        Demo mode¦డెమో మోడ్¦डेमो मोड¦ಡೆಮೊ ಮೋಡ್¦ഡെമോ മോഡ്¦செய்முறை
        Dashboard¦డ్యాష్‌బోర్డ్¦डैशबोर्ड¦ಡ್ಯಾಶ್‌ಬೋರ್ಡ್¦ഡാഷ്ബോർഡ്¦டாஷ்போர்டு
        Products¦ఉత్పత్తులు¦उत्पाद¦ಉತ್ಪನ್ನಗಳು¦ഉൽപ്പന്നങ്ങൾ¦பொருட்கள்
        Orders¦ఆర్డర్లు¦ऑर्डर¦ಆದೇಶಗಳು¦ഓർഡറുകൾ¦ஆர்டர்கள்
        Profile¦ప్రొఫైల్¦प्रोफ़ाइल¦ಪ್ರೊಫೈಲ್¦പ്രൊഫൈൽ¦சுயவிவரம்
        Alerts¦సూచనలు¦सूचनाएँ¦ಸೂಚನೆಗಳು¦അറിയിപ്പുകൾ¦அறிவிப்புகள்
        Notifications¦నోటిఫికేషన్లు¦सूचनाएँ¦ಅಧಿಸೂಚನೆಗಳು¦അറിയിപ്പുകൾ¦அறிவிப்புகள்
        Shop¦దుకాణం¦दुकान¦ಅಂಗಡಿ¦കട¦கடை
        Cart¦కార్ట్¦कार्ट¦ಕಾರ್ಟ್¦കാർട്ട്¦வண்டி
        Farm2Market Farmer¦Farm2Market రైతు¦Farm2Market किसान¦Farm2Market ರೈತ¦Farm2Market കർഷകൻ¦Farm2Market விவசாயி
        Fresh produce,¦తాజా పంటలు,¦ताज़ी उपज,¦ತಾಜಾ ಉತ್ಪನ್ನಗಳು,¦പുതിയ വിളകൾ,¦புதிய விளைபொருட்கள்,
        straight from\nnearby farms 🌾¦సమీపంలోని పొలాల నుండి నేరుగా 🌾¦नज़दीकी खेतों से सीधे 🌾¦ಹತ್ತಿರದ ಹೊಲಗಳಿಂದ ನೇರವಾಗಿ 🌾¦സമീപത്തെ കൃഷിയിടങ്ങളിൽ നിന്ന് നേരിട്ട് 🌾¦அருகிலுள்ள பண்ணைகளிலிருந்து நேரடியாக 🌾
        Showing products within 20 km¦20 కి.మీ. పరిధిలోని ఉత్పత్తులు¦20 किमी के भीतर के उत्पाद¦20 ಕಿ.ಮೀ ವ್ಯಾಪ್ತಿಯ ಉತ್ಪನ್ನಗಳು¦20 കി.മീ പരിധിയിലുള്ള ഉൽപ്പന്നങ്ങൾ¦20 கி.மீ.க்குள் உள்ள பொருட்கள்
        Set your location to browse nearby farms.¦సమీపంలోని పొలాలను చూడటానికి మీ స్థానాన్ని సెట్ చేయండి.¦नज़दीकी खेत देखने के लिए अपना स्थान सेट करें।¦ಹತ್ತಿರದ ಹೊಲಗಳನ್ನು ನೋಡಲು ನಿಮ್ಮ ಸ್ಥಳವನ್ನು ಹೊಂದಿಸಿ.¦സമീപത്തെ കൃഷിയിടങ്ങൾ കാണാൻ നിങ്ങളുടെ ലൊക്കേഷൻ സജ്ജമാക്കുക.¦அருகிலுள்ள பண்ணைகளைப் பார்க்க உங்கள் இருப்பிடத்தை அமைக்கவும்.
        View cart →¦కార్ట్ చూడండి →¦कार्ट देखें →¦ಕಾರ್ಟ್ ವೀಕ್ಷಿಸಿ →¦കാർട്ട് കാണുക →¦வண்டியைப் பார்க்க →
        Search produce…¦పంటల కోసం వెతకండి…¦उपज खोजें…¦ಉತ್ಪನ್ನಗಳನ್ನು ಹುಡುಕಿ…¦വിളകൾ തിരയുക…¦பொருட்களைத் தேடுங்கள்…
        All¦అన్నీ¦सभी¦ಎಲ್ಲಾ¦എല്ലാം¦அனைத்தும்
        Vegetables¦కూరగాయలు¦सब्ज़ियाँ¦ತರಕಾರಿಗಳು¦പച്ചക്കറികൾ¦காய்கறிகள்
        Fruits¦పండ్లు¦फल¦ಹಣ್ಣುಗಳು¦പഴങ്ങൾ¦பழங்கள்
        Leafy Greens¦ఆకుకూరలు¦हरी पत्तेदार सब्ज़ियाँ¦ಸೊಪ್ಪುಗಳು¦ഇലക്കറികൾ¦கீரைகள்
        Grains¦ధాన్యాలు¦अनाज¦ಧಾನ್ಯಗಳು¦ധാന്യങ്ങൾ¦தானியங்கள்
        No products found¦ఉత్పత్తులు కనబడలేదు¦कोई उत्पाद नहीं मिला¦ಉತ್ಪನ್ನಗಳು ಕಂಡುಬಂದಿಲ್ಲ¦ഉൽപ്പന്നങ്ങളൊന്നും കണ്ടെത്തിയില്ല¦பொருட்கள் எதுவும் இல்லை
        Try a different search term.¦వేరే పదంతో వెతకండి.¦दूसरा शब्द खोजें।¦ಬೇರೆ ಪದದಿಂದ ಹುಡುಕಿ.¦മറ്റൊരു വാക്ക് തിരയുക.¦வேறு சொல்லைத் தேடுங்கள்.
        No produce available in this category yet.¦ఈ విభాగంలో ఇంకా ఉత్పత్తులు లేవు.¦इस श्रेणी में अभी उपज उपलब्ध नहीं है।¦ಈ ವಿಭಾಗದಲ್ಲಿ ಇನ್ನೂ ಉತ್ಪನ್ನಗಳಿಲ್ಲ.¦ഈ വിഭാഗത്തിൽ ഇപ്പോൾ ഉൽപ്പന്നങ്ങളില്ല.¦இந்தப் பிரிவில் இன்னும் பொருட்கள் இல்லை.
        Seller: {name}¦విక్రేత: {name}¦विक्रेता: {name}¦ಮಾರಾಟಗಾರ: {name}¦വിൽപ്പനക്കാരൻ: {name}¦விற்பனையாளர்: {name}
        Sold out¦స్టాక్ లేదు¦स्टॉक ख़त्म¦ಸ್ಟಾಕ್ ಇಲ್ಲ¦സ്റ്റോക്ക് തീർന്നു¦கையிருப்பு இல்லை
        Add¦జోడించండి¦जोड़ें¦ಸೇರಿಸಿ¦ചേർക്കുക¦சேர்க்கவும்
        Your cart is empty¦మీ కార్ట్ ఖాళీగా ఉంది¦आपका कार्ट खाली है¦ನಿಮ್ಮ ಕಾರ್ಟ್ ಖಾಲಿಯಾಗಿದೆ¦നിങ്ങളുടെ കാർട്ട് കാലിയാണ്¦உங்கள் வண்டி காலியாக உள்ளது
        Browse the shop and add fresh produce to get started.¦షాపును చూసి తాజా పంటలను జోడించండి.¦शुरू करने के लिए दुकान से ताज़ी उपज जोड़ें।¦ಅಂಗಡಿಗೆ ಹೋಗಿ ತಾಜಾ ಉತ್ಪನ್ನಗಳನ್ನು ಸೇರಿಸಿ.¦തുടങ്ങാൻ കടയിൽ നിന്ന് പുതിയ വിളകൾ ചേർക്കുക.¦கடைக்குச் சென்று புதிய பொருட்களைச் சேர்க்கவும்.
        Your Cart¦మీ కార్ట్¦आपका कार्ट¦ನಿಮ್ಮ ಕಾರ್ಟ್¦നിങ്ങളുടെ കാർട്ട്¦உங்கள் வண்டி
        Clear all¦అన్నీ తొలగించండి¦सब हटाएँ¦ಎಲ್ಲವನ್ನೂ ತೆರವುಗೊಳಿಸಿ¦എല്ലാം നീക്കുക¦அனைத்தையும் அழிக்கவும்
        Remove 1¦1 తొలగించండి¦1 हटाएँ¦1 ತೆಗೆದುಹಾಕಿ¦1 നീക്കുക¦1 நீக்கவும்
        Delivery details¦డెలివరీ వివరాలు¦डिलीवरी विवरण¦ವಿತರಣೆಯ ವಿವರಗಳು¦ഡെലിവറി വിവരങ്ങൾ¦விநியோக விவரங்கள்
        Delivery address¦డెలివరీ చిరునామా¦डिलीवरी पता¦ವಿತರಣಾ ವಿಳಾಸ¦ഡെലിവറി വിലാസം¦விநியோக முகவரி
        Include house or building, street, and area¦ఇల్లు/భవనం, వీధి, ప్రాంతం ఇవ్వండి¦मकान/इमारत, सड़क और इलाका लिखें¦ಮನೆ/ಕಟ್ಟಡ, ರಸ್ತೆ ಮತ್ತು ಪ್ರದೇಶವನ್ನು ನಮೂದಿಸಿ¦വീട്/കെട്ടിടം, തെരുവ്, പ്രദേശം എന്നിവ ചേർക്കുക¦வீடு/கட்டிடம், தெரு, பகுதி ஆகியவற்றை எழுதவும்
        Contact number¦సంప్రదింపు నంబర్¦संपर्क नंबर¦ಸಂಪರ್ಕ ಸಂಖ್ಯೆ¦ബന്ധപ്പെടേണ്ട നമ്പർ¦தொடர்பு எண்
        Order Summary¦ఆర్డర్ సారాంశం¦ऑर्डर सारांश¦ಆರ್ಡರ್ ಸಾರಾಂಶ¦ഓർഡർ സംഗ്രഹം¦ஆர்டர் சுருக்கம்
        Delivery¦డెలివరీ¦डिलीवरी¦ವಿತರಣೆ¦ഡെലിവറി¦விநியோகம்
        Free¦ఉచితం¦मुफ़्त¦ಉಚಿತ¦സൗജന്യം¦இலவசம்
        Total¦మొత్తం¦कुल¦ಒಟ್ಟು¦ആകെ¦மொத்தம்
        Set your location before placing an order.¦ఆర్డర్ చేయడానికి ముందు మీ స్థానాన్ని సెట్ చేయండి.¦ऑर्डर करने से पहले अपना स्थान सेट करें।¦ಆರ್ಡರ್ ಮಾಡುವ ಮೊದಲು ನಿಮ್ಮ ಸ್ಥಳವನ್ನು ಹೊಂದಿಸಿ.¦ഓർഡർ ചെയ്യുന്നതിന് മുമ്പ് ലൊക്കേഷൻ സജ്ജമാക്കുക.¦ஆர்டர் செய்வதற்கு முன் உங்கள் இருப்பிடத்தை அமைக்கவும்.
        No orders yet¦ఇంకా ఆర్డర్లు లేవు¦अभी कोई ऑर्डर नहीं¦ಇನ್ನೂ ಆರ್ಡರ್‌ಗಳಿಲ್ಲ¦ഓർഡറുകളൊന്നുമില്ല¦இன்னும் ஆர்டர்கள் இல்லை
        Your order history will appear here once you place your first order.¦మీ మొదటి ఆర్డర్ తర్వాత ఆర్డర్ చరిత్ర ఇక్కడ కనిపిస్తుంది.¦पहला ऑर्डर देने के बाद आपका इतिहास यहाँ दिखेगा।¦ಮೊದಲ ಆರ್ಡರ್ ಮಾಡಿದ ನಂತರ ನಿಮ್ಮ ಇತಿಹಾಸ ಇಲ್ಲಿ ಕಾಣಿಸುತ್ತದೆ.¦ആദ്യ ഓർഡറിന് ശേഷം ചരിത്രം ഇവിടെ കാണാം.¦முதல் ஆர்டரைச் செய்ததும் வரலாறு இங்கே தெரியும்.
        Your Orders¦మీ ఆర్డర్లు¦आपके ऑर्डर¦ನಿಮ್ಮ ಆರ್ಡರ್‌ಗಳು¦നിങ്ങളുടെ ഓർഡറുകൾ¦உங்கள் ஆர்டர்கள்
        Customer Account¦కస్టమర్ ఖాతా¦ग्राहक खाता¦ಗ್ರಾಹಕರ ಖಾತೆ¦ഉപഭോക്തൃ അക്കൗണ്ട്¦வாடிக்கையாளர் கணக்கு
        Browsing farms within 20 km¦20 కి.మీ. పరిధిలోని పొలాలు¦20 किमी के भीतर के खेत¦20 ಕಿ.ಮೀ ವ್ಯಾಪ್ತಿಯ ಹೊಲಗಳು¦20 കി.മീ പരിധിയിലെ കൃഷിയിടങ്ങൾ¦20 கி.மீ.க்குள் உள்ள பண்ணைகள்
        No location set¦స్థానం సెట్ చేయలేదు¦स्थान सेट नहीं है¦ಸ್ಥಳ ಹೊಂದಿಸಿಲ್ಲ¦ലൊക്കേഷൻ സജ്ജമാക്കിയിട്ടില്ല¦இருப்பிடம் அமைக்கப்படவில்லை
        Update Location¦స్థానాన్ని మార్చండి¦स्थान अपडेट करें¦ಸ್ಥಳವನ್ನು ನವೀಕರಿಸಿ¦ലൊക്കേഷൻ പുതുക്കുക¦இருப்பிடத்தைப் புதுப்பிக்கவும்
        Set Location¦స్థానాన్ని సెట్ చేయండి¦स्थान सेट करें¦ಸ್ಥಳವನ್ನು ಹೊಂದಿಸಿ¦ലൊക്കേഷൻ സജ്ജമാക്കുക¦இருப்பிடத்தை அமைக்கவும்
        Change Name¦పేరును మార్చండి¦नाम बदलें¦ಹೆಸರು ಬದಲಾಯಿಸಿ¦പേര് മാറ്റുക¦பெயரை மாற்றவும்
        Sign Out¦సైన్ అవుట్¦साइन आउट¦ಸೈನ್ ಔಟ್¦സൈൻ ഔട്ട്¦வெளியேறு
        Version 1.0 · Customer App¦వెర్షన్ 1.0 · కస్టమర్ యాప్¦संस्करण 1.0 · ग्राहक ऐप¦ಆವೃತ್ತಿ 1.0 · ಗ್ರಾಹಕ ಆ್ಯಪ್¦പതിപ്പ് 1.0 · കസ്റ്റമർ ആപ്പ്¦பதிப்பு 1.0 · வாடிக்கையாளர் செயலி
        Connected to live marketplace¦లైవ్ మార్కెట్‌ప్లేస్‌కు కనెక్ట్ అయింది¦लाइव मार्केटप्लेस से जुड़ा¦ಲೈವ್ ಮಾರುಕಟ್ಟೆಗೆ ಸಂಪರ್ಕಿಸಲಾಗಿದೆ¦ലൈവ് മാർക്കറ്റുമായി ബന്ധിപ്പിച്ചു¦நேரடி சந்தையுடன் இணைக்கப்பட்டது
        Running in demo mode¦డెమో మోడ్‌లో నడుస్తోంది¦डेमो मोड चल रहा है¦ಡೆಮೊ ಮೋಡ್‌ನಲ್ಲಿ ಕಾರ್ಯನಿರ್ವಹಿಸುತ್ತಿದೆ¦ഡെമോ മോഡിൽ പ്രവർത്തിക്കുന്നു¦செய்முறை நிலையில் இயங்குகிறது
        Welcome back 👨‍🌾¦మళ్లీ స్వాగతం 👨‍🌾¦वापस स्वागत है 👨‍🌾¦ಮತ್ತೆ ಸ್ವಾಗತ 👨‍🌾¦വീണ്ടും സ്വാഗതം 👨‍🌾¦மீண்டும் வருக 👨‍🌾
        Here's a snapshot of your farm.¦మీ పొలం వివరాలు ఇక్కడ ఉన్నాయి.¦आपके खेत की जानकारी यहाँ है।¦ನಿಮ್ಮ ಫಾರ್ಮ್‌ನ ವಿವರಗಳು ಇಲ್ಲಿವೆ.¦നിങ്ങളുടെ കൃഷിയിടത്തിന്റെ വിവരങ്ങൾ ഇതാ.¦உங்கள் பண்ணையின் விவரங்கள் இங்கே.
        In stock¦స్టాక్‌లో ఉంది¦स्टॉक में¦ಸ್ಟಾಕ್‌ನಲ್ಲಿದೆ¦സ്റ്റോക്കിലുണ്ട്¦கையிருப்பில் உள்ளது
        Total Revenue¦మొత్తం ఆదాయం¦कुल आय¦ಒಟ್ಟು ಆದಾಯ¦ആകെ വരുമാനം¦மொத்த வருவாய்
        from delivered orders¦డెలివరీ అయిన ఆర్డర్ల నుండి¦डिलीवर किए गए ऑर्डर से¦ವಿತರಿಸಿದ ಆರ್ಡರ್‌ಗಳಿಂದ¦ഡെലിവറി ചെയ്ത ഓർഡറുകളിൽ നിന്ന്¦விநியோகித்த ஆர்டர்களிலிருந்து
        Tap to review and accept¦పరిశీలించి అంగీకరించడానికి నొక్కండి¦जाँचकर स्वीकार करने के लिए टैप करें¦ಪರಿಶೀಲಿಸಿ ಸ್ವೀಕರಿಸಲು ಟ್ಯಾಪ್ ಮಾಡಿ¦പരിശോധിച്ച് സ്വീകരിക്കാൻ ടാപ്പ് ചെയ്യുക¦சரிபார்த்து ஏற்கத் தட்டவும்
        Recent Orders¦ఇటీవలి ఆర్డర్లు¦हाल के ऑर्डर¦ಇತ್ತೀಚಿನ ಆರ್ಡರ್‌ಗಳು¦സമീപകാല ഓർഡറുകൾ¦சமீபத்திய ஆர்டர்கள்
        Orders from customers will appear here.¦కస్టమర్ల ఆర్డర్లు ఇక్కడ కనిపిస్తాయి.¦ग्राहकों के ऑर्डर यहाँ दिखेंगे।¦ಗ್ರಾಹಕರ ಆರ್ಡರ್‌ಗಳು ಇಲ್ಲಿ ಕಾಣಿಸುತ್ತವೆ.¦ഉപഭോക്താക്കളുടെ ഓർഡറുകൾ ഇവിടെ കാണാം.¦வாடிக்கையாளர் ஆர்டர்கள் இங்கே தெரியும்.
        My Products¦నా ఉత్పత్తులు¦मेरे उत्पाद¦ನನ್ನ ಉತ್ಪನ್ನಗಳು¦എന്റെ ഉൽപ്പന്നങ്ങൾ¦எனது பொருட்கள்
        Cancel¦రద్దు¦रद्द करें¦ರದ್ದುಮಾಡಿ¦റദ്ദാക്കുക¦ரத்து செய்
        Add Product¦ఉత్పత్తిని జోడించండి¦उत्पाद जोड़ें¦ಉತ್ಪನ್ನ ಸೇರಿಸಿ¦ഉൽപ്പന്നം ചേർക്കുക¦பொருளைச் சேர்க்கவும்
        New Listing¦కొత్త లిస్టింగ్¦नई सूची¦ಹೊಸ ಪಟ್ಟಿ¦പുതിയ ലിസ്റ്റിംഗ്¦புதிய பட்டியல்
        Product name¦ఉత్పత్తి పేరు¦उत्पाद का नाम¦ಉತ್ಪನ್ನದ ಹೆಸರು¦ഉൽപ്പന്നത്തിന്റെ പേര്¦பொருளின் பெயர்
        Price ₹/kg¦ధర ₹/కిలో¦कीमत ₹/किलो¦ಬೆಲೆ ₹/ಕೆ.ಜಿ.¦വില ₹/കിലോ¦விலை ₹/கிலோ
        Stock (kg)¦స్టాక్ (కిలోలు)¦स्टॉक (किलो)¦ಸ್ಟಾಕ್ (ಕೆ.ಜಿ.)¦സ്റ്റോക്ക് (കിലോ)¦இருப்பு (கிலோ)
        Choose product picture¦ఉత్పత్తి ఫోటో ఎంచుకోండి¦उत्पाद की तस्वीर चुनें¦ಉತ್ಪನ್ನದ ಫೋಟೋ ಆಯ್ಕೆಮಾಡಿ¦ഉൽപ്പന്നത്തിന്റെ ചിത്രം തിരഞ്ഞെടുക്കുക¦பொருளின் படத்தைத் தேர்ந்தெடுக்கவும்
        Picture selected · Change¦ఫోటో ఎంచుకున్నారు · మార్చండి¦तस्वीर चुनी · बदलें¦ಫೋಟೋ ಆಯ್ಕೆಮಾಡಲಾಗಿದೆ · ಬದಲಿಸಿ¦ചിത്രം തിരഞ്ഞെടുത്തു · മാറ്റുക¦படம் தேர்ந்தெடுக்கப்பட்டது · மாற்று
        Publish Listing¦లిస్టింగ్‌ను ప్రచురించండి¦लिस्टिंग प्रकाशित करें¦ಪಟ್ಟಿಯನ್ನು ಪ್ರಕಟಿಸಿ¦ലിസ്റ്റിംഗ് പ്രസിദ്ധീകരിക്കുക¦பட்டியலை வெளியிடவும்
        Choose a product photo before publishing.¦ప్రచురించే ముందు ఉత్పత్తి ఫోటో ఎంచుకోండి.¦प्रकाशित करने से पहले उत्पाद की तस्वीर चुनें।¦ಪ್ರಕಟಿಸುವ ಮೊದಲು ಉತ್ಪನ್ನದ ಫೋಟೋ ಆಯ್ಕೆಮಾಡಿ.¦പ്രസിദ്ധീകരിക്കുന്നതിന് മുമ്പ് ഉൽപ്പന്നത്തിന്റെ ചിത്രം തിരഞ്ഞെടുക്കുക.¦வெளியிடும் முன் பொருளின் படத்தைத் தேர்ந்தெடுக்கவும்.
        New orders will appear here.¦కొత్త ఆర్డర్లు ఇక్కడ కనిపిస్తాయి.¦नए ऑर्डर यहाँ दिखेंगे।¦ಹೊಸ ಆರ್ಡರ್‌ಗಳು ಇಲ್ಲಿ ಕಾಣಿಸುತ್ತವೆ.¦പുതിയ ഓർഡറുകൾ ഇവിടെ കാണാം.¦புதிய ஆர்டர்கள் இங்கே தெரியும்.
        Nothing to show here yet.¦ఇక్కడ ఇంకా ఏమీ లేదు.¦अभी यहाँ कुछ नहीं है।¦ಇಲ್ಲಿ ಇನ್ನೂ ಏನೂ ಇಲ್ಲ.¦ഇവിടെ ഇപ്പോൾ ഒന്നുമില്ല.¦இங்கே இன்னும் எதுவும் இல்லை.
        Mark sold out¦స్టాక్ అయిపోయింది అని గుర్తించండి¦स्टॉक ख़त्म चिह्नित करें¦ಸ್ಟಾಕ್ ಮುಗಿದಿದೆ ಎಂದು ಗುರುತಿಸಿ¦സ്റ്റോക്ക് തീർന്നതായി അടയാളപ്പെടുത്തുക¦கையிருப்பு தீர்ந்தது எனக் குறிக்கவும்
        Adjust stock:¦స్టాక్ మార్చండి:¦स्टॉक बदलें:¦ಸ್ಟಾಕ್ ಹೊಂದಿಸಿ:¦സ്റ്റോക്ക് ക്രമീകരിക്കുക:¦இருப்பை மாற்று:
        Relist¦మళ్లీ జాబితా చేయండి¦फिर से सूचीबद्ध करें¦ಮತ್ತೆ ಪಟ್ಟಿ ಮಾಡಿ¦വീണ്ടും ലിസ്റ്റ് ചെയ്യുക¦மீண்டும் பட்டியலிடு
        Edit name, type, price or stock¦పేరు, రకం, ధర లేదా స్టాక్ మార్చండి¦नाम, प्रकार, कीमत या स्टॉक बदलें¦ಹೆಸರು, ಪ್ರಕಾರ, ಬೆಲೆ ಅಥವಾ ಸ್ಟಾಕ್ ಬದಲಿಸಿ¦പേര്, തരം, വില അല്ലെങ്കിൽ സ്റ്റോക്ക് തിരുത്തുക¦பெயர், வகை, விலை அல்லது இருப்பைத் திருத்து
        Edit product¦ఉత్పత్తిని సవరించండి¦उत्पाद संपादित करें¦ಉತ್ಪನ್ನ ತಿದ್ದು¦ഉൽപ്പന്നം തിരുത്തുക¦பொருளைத் திருத்து
        Change product photo¦ఉత్పత్తి ఫోటో మార్చండి¦उत्पाद की तस्वीर बदलें¦ಉತ್ಪನ್ನದ ಫೋಟೋ ಬದಲಿಸಿ¦ഉൽപ്പന്നത്തിന്റെ ചിത്രം മാറ്റുക¦பொருளின் படத்தை மாற்று
        Add product photo¦ఉత్పత్తి ఫోటో జోడించండి¦उत्पाद की तस्वीर जोड़ें¦ಉತ್ಪನ್ನದ ಫೋಟೋ ಸೇರಿಸಿ¦ഉൽപ്പന്നത്തിന്റെ ചിത്രം ചേർക്കുക¦பொருளின் படத்தைச் சேர்க்கவும்
        Photo selected · Change¦ఫోటో ఎంచుకున్నారు · మార్చండి¦तस्वीर चुनी · बदलें¦ಫೋಟೋ ಆಯ್ಕೆಮಾಡಲಾಗಿದೆ · ಬದಲಿಸಿ¦ചിത്രം തിരഞ്ഞെടുത്തു · മാറ്റുക¦படம் தேர்ந்தெடுக்கப்பட்டது · மாற்று
        Save changes¦మార్పులను సేవ్ చేయండి¦बदलाव सहेजें¦ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ¦മാറ്റങ്ങൾ സംരക്ഷിക്കുക¦மாற்றங்களைச் சேமி
        Product updated¦ఉత్పత్తి నవీకరించబడింది¦उत्पाद अपडेट हुआ¦ಉತ್ಪನ್ನ ನವೀಕರಿಸಲಾಗಿದೆ¦ഉൽപ്പന്നം പുതുക്കി¦பொருள் புதுப்பிக்கப்பட்டது
        Active¦యాక్టివ్¦सक्रिय¦ಸಕ್ರಿಯ¦സജീവം¦செயலில்
        Placed¦చేయబడింది¦दिया गया¦ನೀಡಲಾಗಿದೆ¦നൽകി¦வைக்கப்பட்டது
        Preparing¦తయారవుతోంది¦तैयार हो रहा है¦ತಯಾರಾಗುತ್ತಿದೆ¦തയ്യാറാകുന്നു¦தயாராகிறது
        On the way¦మార్గంలో ఉంది¦रास्ते में¦ದಾರಿಯಲ್ಲಿದೆ¦വഴിയിലാണ്¦வழியில் உள்ளது
        Delivered¦డెలివరీ అయింది¦डिलीवर हो गया¦ವಿತರಿಸಲಾಗಿದೆ¦ഡെലിവറി ചെയ്തു¦விநியோகிக்கப்பட்டது
        Notifications¦నోటిఫికేషన్లు¦सूचनाएँ¦ಅಧಿಸೂಚನೆಗಳು¦അറിയിപ്പുകൾ¦அறிவிப்புகள்
        No notifications¦నోటిఫికేషన్లు లేవు¦कोई सूचना नहीं¦ಯಾವುದೇ ಅಧಿಸೂಚನೆಗಳಿಲ್ಲ¦അറിയിപ്പുകളൊന്നുമില്ല¦அறிவிப்புகள் இல்லை
        Order updates will appear here.¦ఆర్డర్ అప్‌డేట్‌లు ఇక్కడ కనిపిస్తాయి.¦ऑर्डर अपडेट यहाँ दिखेंगे।¦ಆರ್ಡರ್ ಅಪ್‌ಡೇಟ್‌ಗಳು ಇಲ್ಲಿ ಕಾಣಿಸುತ್ತವೆ.¦ഓർഡർ അപ്‌ഡേറ്റുകൾ ഇവിടെ കാണാം.¦ஆர்டர் புதுப்பிப்புகள் இங்கே தெரியும்.
        Finish setting up your farmer profile.¦మీ రైతు ప్రొఫైల్ సెటప్‌ను పూర్తి చేయండి.¦अपनी किसान प्रोफ़ाइल सेट करें।¦ನಿಮ್ಮ ರೈತ ಪ್ರೊಫೈಲ್ ಹೊಂದಿಸುವುದನ್ನು ಪೂರ್ಣಗೊಳಿಸಿ.¦നിങ്ങളുടെ കർഷക പ്രൊഫൈൽ സജ്ജീകരണം പൂർത്തിയാക്കുക.¦உங்கள் விவசாயி சுயவிவர அமைப்பை முடிக்கவும்.
        Finish setting up your customer profile.¦మీ కస్టమర్ ప్రొఫైల్ సెటప్‌ను పూర్తి చేయండి.¦अपनी ग्राहक प्रोफ़ाइल सेट करें।¦ನಿಮ್ಮ ಗ್ರಾಹಕ ಪ್ರೊಫೈಲ್ ಹೊಂದಿಸುವುದನ್ನು ಪೂರ್ಣಗೊಳಿಸಿ.¦നിങ്ങളുടെ ഉപഭോക്തൃ പ്രൊഫൈൽ സജ്ജീകരണം പൂർത്തിയാക്കുക.¦உங்கள் வாடிக்கையாளர் சுயவிவர அமைப்பை முடிக்கவும்.
        Manage your farm and receive nearby customer orders.¦మీ పొలాన్ని నిర్వహించి సమీప కస్టమర్ల ఆర్డర్లను పొందండి.¦अपने खेत का प्रबंधन करें और आस-पास के ग्राहकों के ऑर्डर पाएँ।¦ನಿಮ್ಮ ಫಾರ್ಮ್ ನಿರ್ವಹಿಸಿ, ಹತ್ತಿರದ ಗ್ರಾಹಕರ ಆರ್ಡರ್‌ಗಳನ್ನು ಪಡೆಯಿರಿ.¦നിങ്ങളുടെ കൃഷിയിടം നിയന്ത്രിച്ച് സമീപത്തെ ഓർഡറുകൾ സ്വീകരിക്കുക.¦உங்கள் பண்ணையை நிர்வகித்து அருகிலுள்ள வாடிக்கையாளர் ஆர்டர்களைப் பெறுங்கள்.
        Sign in to find fresh produce from nearby farms.¦సమీప పొలాల తాజా పంటలను చూడటానికి సైన్ ఇన్ చేయండి.¦नज़दीकी खेतों की ताज़ी उपज देखने के लिए साइन इन करें।¦ಹತ್ತಿರದ ಹೊಲಗಳ ತಾಜಾ ಉತ್ಪನ್ನಗಳನ್ನು ನೋಡಲು ಸೈನ್ ಇನ್ ಮಾಡಿ.¦സമീപത്തെ കൃഷിയിടങ്ങളിലെ പുതിയ വിളകൾ കാണാൻ സൈൻ ഇൻ ചെയ്യുക.¦அருகிலுள்ள பண்ணைகளின் புதிய பொருட்களைப் பார்க்க உள்நுழையவும்.
        Profile details¦ప్రొఫైల్ వివరాలు¦प्रोफ़ाइल विवरण¦ಪ್ರೊಫೈಲ್ ವಿವರಗಳು¦പ്രൊഫൈൽ വിവരങ്ങൾ¦சுயவிவர விவரங்கள்
        Create your account¦మీ ఖాతాను సృష్టించండి¦अपना खाता बनाएँ¦ನಿಮ್ಮ ಖಾತೆ ರಚಿಸಿ¦നിങ്ങളുടെ അക്കൗണ്ട് സൃഷ്ടിക്കുക¦உங்கள் கணக்கை உருவாக்கவும்
        Sign in¦సైన్ ఇన్¦साइन इन¦ಸೈನ್ ಇನ್¦സൈൻ ഇൻ¦உள்நுழை
        Sign up¦సైన్ అప్¦साइन अप¦ಸೈನ್ ಅಪ್¦സൈൻ അപ്പ്¦பதிவு செய்
        Full name¦పూర్తి పేరు¦पूरा नाम¦ಪೂರ್ಣ ಹೆಸರು¦പൂർണ്ണ പേര്¦முழுப் பெயர்
        Email address¦ఇమెయిల్ చిరునామా¦ईमेल पता¦ಇಮೇಲ್ ವಿಳಾಸ¦ഇമെയിൽ വിലാസം¦மின்னஞ்சல் முகவரி
        Password¦పాస్‌వర్డ్¦पासवर्ड¦ಪಾಸ್‌ವರ್ಡ್¦പാസ്‌വേഡ്¦கடவுச்சொல்
        Use at least 8 characters. Email accounts may need to confirm the link sent by Supabase.¦కనీసం 8 అక్షరాలు వాడండి. ఇమెయిల్‌లో వచ్చిన లింక్‌ను నిర్ధారించాల్సి రావచ్చు.¦कम से कम 8 अक्षर रखें। ईमेल से आए लिंक की पुष्टि करनी पड़ सकती है।¦ಕನಿಷ್ಠ 8 ಅಕ್ಷರ ಬಳಸಿ. ಇಮೇಲ್‌ನ ಲಿಂಕ್ ದೃಢೀಕರಿಸಬೇಕಾಗಬಹುದು.¦കുറഞ്ഞത് 8 അക്ഷരങ്ങൾ വേണം. ഇമെയിലിലെ ലിങ്ക് സ്ഥിരീകരിക്കേണ്ടി വരാം.¦குறைந்தது 8 எழுத்துகள் பயன்படுத்தவும். மின்னஞ்சல் இணைப்பை உறுதிப்படுத்த வேண்டியிருக்கலாம்.
        Set location for nearby orders¦ಹತ್ತಿರದ ಆರ್ಡರ್‌ಗಳಿಗಾಗಿ ಸ್ಥಳವನ್ನು ಹೊಂದಿಸಿ¦आस-पास के ऑर्डर के लिए स्थान सेट करें¦ಹತ್ತಿರದ ಆರ್ಡರ್‌ಗಳಿಗಾಗಿ ಸ್ಥಳ ಹೊಂದಿಸಿ¦സമീപത്തെ ഓർഡറുകൾക്കായി ലൊക്കേഷൻ സജ്ജമാക്കുക¦அருகிலுள்ள ஆர்டர்களுக்கு இருப்பிடத்தை அமைக்கவும்
        Location ready¦స్థానం సిద్ధంగా ఉంది¦स्थान तैयार है¦ಸ್ಥಳ ಸಿದ್ಧವಾಗಿದೆ¦ലൊക്കേഷൻ തയ്യാറാണ്¦இருப்பிடம் தயார்
        Please wait…¦దయచేసి వేచి ఉండండి…¦कृपया प्रतीक्षा करें…¦ದಯವಿಟ್ಟು ಕಾಯಿರಿ…¦ദയവായി കാത്തിരിക്കുക…¦காத்திருக்கவும்…
        Save and continue¦సేవ్ చేసి కొనసాగించండి¦सहेजें और आगे बढ़ें¦ಉಳಿಸಿ ಮುಂದುವರಿಯಿರಿ¦സംരക്ഷിച്ച് തുടരുക¦சேமித்து தொடரவும்
        Create account¦ఖాతా సృష్టించండి¦खाता बनाएँ¦ಖಾತೆ ರಚಿಸಿ¦അക്കൗണ്ട് സൃഷ്ടിക്കുക¦கணக்கை உருவாக்கு
        Continue in demo mode¦డెమో మోడ్‌లో కొనసాగండి¦डेमो मोड में जारी रखें¦ಡೆಮೊ ಮೋಡ್‌ನಲ್ಲಿ ಮುಂದುವರಿಯಿರಿ¦ഡെമോ മോഡിൽ തുടരുക¦செய்முறை நிலையில் தொடரவும்
        Account created. Confirm the link in your email, then sign in with your password.¦ఖాతా సృష్టించబడింది. ఇమెయిల్ లింక్‌ను నిర్ధారించి, పాస్‌వర్డ్‌తో సైన్ ఇన్ చేయండి.¦खाता बन गया। ईमेल लिंक की पुष्टि करके पासवर्ड से साइन इन करें।¦ಖಾತೆ ರಚಿಸಲಾಗಿದೆ. ಇಮೇಲ್ ಲಿಂಕ್ ದೃಢೀಕರಿಸಿ, ಪಾಸ್‌ವರ್ಡ್‌ನೊಂದಿಗೆ ಸೈನ್ ಇನ್ ಮಾಡಿ.¦അക്കൗണ്ട് സൃഷ്ടിച്ചു. ഇമെയിൽ ലിങ്ക് സ്ഥിരീകരിച്ച് പാസ്‌വേഡ് ഉപയോഗിച്ച് സൈൻ ഇൻ ചെയ്യുക.¦கணக்கு உருவாக்கப்பட்டது. மின்னஞ்சல் இணைப்பை உறுதிசெய்து கடவுச்சொல்லுடன் உள்நுழையவும்.
        Email confirmed. Continue to finish setting up your account.¦ഇമെയിൽ സ്ഥിരീകരിച്ചു. അക്കൗണ്ട് ക്രമീകരണം പൂർത്തിയാക്കുക.¦ईमेल की पुष्टि हुई। खाता सेटअप पूरा करें।¦ಇಮೇಲ್ ದೃಢೀಕರಿಸಲಾಗಿದೆ. ಖಾತೆ ಹೊಂದಿಸುವುದನ್ನು ಪೂರ್ಣಗೊಳಿಸಿ.¦ഇമെയിൽ സ്ഥിരീകരിച്ചു. അക്കൗണ്ട് സജ്ജീകരണം പൂർത്തിയാക്കുക.¦மின்னஞ்சல் உறுதிசெய்யப்பட்டது. கணக்கு அமைப்பை முடிக்கவும்.
        Signed out¦സൈൻ ഔട്ട് ചെയ്തു¦साइन आउट हो गया¦ಸೈನ್ ಔಟ್ ಆಗಿದೆ¦സൈൻ ഔട്ട് ചെയ്തു¦வெளியேறிவிட்டீர்கள்
        Could not read location — ensure device GPS is on.¦സ്ഥാനം വായിക്കാനായില്ല — GPS ഓണാണെന്ന് ഉറപ്പാക്കുക.¦स्थान नहीं मिला — डिवाइस GPS चालू करें।¦ಸ್ಥಳ ಓದಲಾಗಲಿಲ್ಲ — GPS ಆನ್ ಇದೆಯೇ ನೋಡಿ.¦ലൊക്കേഷൻ ലഭിച്ചില്ല — GPS ഓണാണെന്ന് ഉറപ്പാക്കുക.¦இருப்பிடத்தைப் படிக்க முடியவில்லை — GPS இயக்கப்பட்டுள்ளதா பார்க்கவும்.
        Location permission is required for the 20 km marketplace.¦20 కి.మీ. మార్కెట్‌ప్లేస్‌కు స్థాన అనుమతి అవసరం.¦20 किमी मार्केटप्लेस के लिए स्थान अनुमति ज़रूरी है।¦20 ಕಿ.ಮೀ ಮಾರುಕಟ್ಟೆಗೆ ಸ್ಥಳ ಅನುಮತಿ ಅಗತ್ಯ.¦20 കി.മീ മാർക്കറ്റിന് ലൊക്കേഷൻ അനുമതി ആവശ്യമാണ്.¦20 கி.மீ. சந்தைக்கு இருப்பிட அனுமதி தேவை.
        ⏳ Pending¦⏳ పెండింగ్¦⏳ लंबित¦⏳ ಬಾಕಿ¦⏳ കാത്തിരിക്കുന്നു¦⏳ நிலுவையில்
        ✅ Accepted¦✅ ఆమోదించబడింది¦✅ स्वीकार किया¦✅ ಸ್ವೀಕರಿಸಲಾಗಿದೆ¦✅ സ്വീകരിച്ചു¦✅ ஏற்கப்பட்டது
        📦 Ready¦📦 సిద్ధంగా ఉంది¦📦 तैयार¦📦 ಸಿದ್ಧವಾಗಿದೆ¦📦 തയ്യാറായി¦📦 தயாராக உள்ளது
        🚚 On the way¦🚚 మార్గంలో ఉంది¦🚚 रास्ते में¦🚚 ದಾರಿಯಲ್ಲಿದೆ¦🚚 വഴിയിലാണ്¦🚚 வழியில் உள்ளது
        🎉 Delivered¦🎉 అందించబడింది¦🎉 डिलीवर हो गया¦🎉 ತಲುಪಿಸಲಾಗಿದೆ¦🎉 എത്തിച്ചു¦🎉 விநியோகிக்கப்பட்டது
        New order received¦కొత్త ఆర్డర్ వచ్చింది¦नया ऑर्डर मिला¦ಹೊಸ ಆರ್ಡರ್ ಬಂದಿದೆ¦പുതിയ ഓർഡർ ലഭിച്ചു¦புதிய ஆர்டர் வந்துள்ளது
        Order update¦ఆర్డర్ అప్‌డేట్¦ऑर्डर अपडेट¦ಆರ್ಡರ್ ಅಪ್‌ಡೇಟ್¦ഓർഡർ അപ്‌ഡേറ്റ്¦ஆர்டர் புதுப்பிப்பு
        Order #{id} is {status}.¦ఆర్డర్ #{id}: {status}.¦ऑर्डर #{id} {status}।¦ಆರ್ಡರ್ #{id} {status}.¦ഓർഡർ #{id} {status}.¦ஆர்டர் #{id} {status}.
        {count} item(s) in cart¦కార్ట్‌లో {count} వస్తువులు¦कार्ट में {count} आइटम¦ಕಾರ್ಟ್‌ನಲ್ಲಿ {count} ವಸ್ತುಗಳು¦കാർട്ടിൽ {count} ഇനങ്ങൾ¦வண்டியில் {count} பொருட்கள்
        {count} product(s) available¦{count} ఉత్పత్తులు అందుబాటులో¦{count} उत्पाद उपलब्ध¦{count} ಉತ್ಪನ್ನಗಳು ಲಭ್ಯ¦{count} ഉൽപ്പന്നങ്ങൾ ലഭ്യമാണ്¦{count} பொருட்கள் கிடைக்கின்றன
        {count} new order(s) waiting¦{count} కొత్త ఆర్డర్లు వేచి ఉన్నాయి¦{count} नए ऑर्डर प्रतीक्षा में¦{count} ಹೊಸ ಆರ್ಡರ್‌ಗಳು ಕಾಯುತ್ತಿವೆ¦{count} പുതിയ ഓർഡറുകൾ കാത്തിരിക്കുന്നു¦{count} புதிய ஆர்டர்கள் காத்திருக்கின்றன
        View all {count} orders →¦అన్ని {count} ఆర్డర్లు చూడండి →¦सभी {count} ऑर्डर देखें →¦ಎಲ್ಲಾ {count} ಆರ್ಡರ್‌ಗಳನ್ನು ನೋಡಿ →¦എല്ലാ {count} ഓർഡറുകളും കാണുക →¦அனைத்து {count} ஆர்டர்களையும் பார்க்க →
        Items ({count})¦వస్తువులు ({count})¦आइटम ({count})¦ವಸ್ತುಗಳು ({count})¦ഇനങ്ങൾ ({count})¦பொருட்கள் ({count})
        {count} kg available¦{count} కిలోలు అందుబాటులో¦{count} किलो उपलब्ध¦{count} ಕೆ.ಜಿ. ಲಭ್ಯ¦{count} കിലോ ലഭ്യമാണ്¦{count} கிலோ கிடைக்கிறது
        Delivery address: {value}¦డెలివరీ చిరునామా: {value}¦डिलीवरी पता: {value}¦ವಿತರಣಾ ವಿಳಾಸ: {value}¦ഡെലിവറി വിലാസം: {value}¦விநியோக முகவரி: {value}
        Customer contact: {value}¦కస్టమర్ ఫోన్: {value}¦ग्राहक संपर्क: {value}¦ಗ್ರಾಹಕರ ಸಂಪರ್ಕ: {value}¦ഉപഭോക്തൃ ബന്ധപ്പെടേണ്ട നമ്പർ: {value}¦வாடிக்கையாளர் தொடர்பு: {value}
        Customer: {value}¦కస్టమర్: {value}¦ग्राहक: {value}¦ಗ್ರಾಹಕ: {value}¦ഉപഭോക്താവ്: {value}¦வாடிக்கையாளர்: {value}
        Estimated delivery by {value} (within 24 hours)¦అంచనా డెలివరీ: {value} (24 గంటల్లో)¦अनुमानित डिलीवरी: {value} (24 घंटे के भीतर)¦ಅಂದಾಜು ವಿತರಣೆ: {value} (24 ಗಂಟೆಗಳೊಳಗೆ)¦പ്രതീക്ഷിക്കുന്ന ഡെലിവറി: {value} (24 മണിക്കൂറിനകം)¦எதிர்பார்க்கும் விநியோகம்: {value} (24 மணிநேரத்திற்குள்)
        {name} added to cart 🛒¦{name} కార్ట్‌లో చేర్చబడింది 🛒¦{name} कार्ट में जोड़ा गया 🛒¦{name} ಕಾರ್ಟ್‌ಗೆ ಸೇರಿಸಲಾಗಿದೆ 🛒¦{name} കാർട്ടിൽ ചേർത്തു 🛒¦{name} வண்டியில் சேர்க்கப்பட்டது 🛒
        Order #{id}¦ఆర్డర్ #{id}¦ऑर्डर #{id}¦ಆರ್ಡರ್ #{id}¦ഓർഡർ #{id}¦ஆர்டர் #{id}
        pending¦పెండింగ్¦लंबित¦ಬಾಕಿ¦കാത്തിരിക്കുന്നു¦நிலுவையில்
        accepted¦ఆమోదించబడింది¦स्वीकार किया¦ಸ್ವೀಕರಿಸಲಾಗಿದೆ¦സ്വീകരിച്ചു¦ஏற்கப்பட்டது
        ready¦సిద్ధంగా ఉంది¦तैयार¦ಸಿದ್ಧವಾಗಿದೆ¦തയ്യാറായി¦தயார்
        out for delivery¦డెలివరీకి బయలుదేరింది¦डिलीवरी के लिए निकला¦ವಿತರಣೆಗೆ ಹೊರಟಿದೆ¦ഡെലിവറിക്കായി പുറപ്പെട്ടു¦விநியோகத்திற்குப் புறப்பட்டது
        delivered¦డెలివరీ అయింది¦डिलीवर हो गया¦ವಿತರಿಸಲಾಗಿದೆ¦ഡെലിവറി ചെയ്തു¦விநியோகிக்கப்பட்டது
        cancelled¦రద్దు చేయబడింది¦रद्द किया गया¦ರದ್ದಾಗಿದೆ¦റദ്ദാക്കി¦ரத்து செய்யப்பட்டது
        In Cart¦కార్ట్‌లో¦कार्ट में¦ಕಾರ್ಟ್‌ನಲ್ಲಿ¦കാർട്ടിൽ¦வண்டியில்
        📍 Location¦📍 స్థానం¦📍 स्थान¦📍 ಸ್ಥಳ¦📍 ലൊക്കേഷൻ¦📍 இருப்பிடம்
        ⚙️ Account¦⚙️ ఖాతా¦⚙️ खाता¦⚙️ ಖಾತೆ¦⚙️ അക്കൗണ്ട്¦⚙️ கணக்கு
        ℹ️ About¦ℹ️ గురించి¦ℹ️ परिचय¦ℹ️ ಬಗ್ಗೆ¦ℹ️ വിവരങ്ങൾ¦ℹ️ பற்றி
        Farmer Account¦రైతు ఖాతా¦किसान खाता¦ರೈತರ ಖಾತೆ¦കർഷക അക്കൗണ്ട്¦விவசாயி கணக்கு
        Version 1.0 · Farmer Portal¦వెర్షన్ 1.0 · రైతు యాప్¦संस्करण 1.0 · किसान ऐप¦ಆವೃತ್ತಿ 1.0 · ರೈತ ಆ್ಯಪ್¦പതിപ്പ് 1.0 · കർഷക ആപ്പ്¦பதிப்பு 1.0 · விவசாயி செயலி
        No products yet¦ఇంకా ఉత్పత్తులు లేవు¦अभी कोई उत्पाद नहीं¦ಇನ್ನೂ ಉತ್ಪನ್ನಗಳಿಲ್ಲ¦ഉൽപ്പന്നങ്ങളൊന്നുമില്ല¦இன்னும் பொருட்கள் இல்லை
        Tap 'Add Product' to create your first listing.¦మొదటి ఉత్పత్తిని జాబితా చేయడానికి 'Add Product' నొక్కండి.¦पहली लिस्टिंग के लिए 'Add Product' टैप करें।¦ಮೊದಲ ಪಟ್ಟಿಗೆ 'Add Product' ಟ್ಯಾಪ್ ಮಾಡಿ.¦ആദ്യ ലിസ്റ്റിംഗിനായി 'Add Product' ടാപ്പ് ചെയ്യുക.¦முதல் பட்டியலுக்கு 'Add Product' என்பதைத் தட்டவும்.
        Enter a product name.¦ఉత్పత్తి పేరు నమోదు చేయండి.¦उत्पाद का नाम लिखें।¦ಉತ್ಪನ್ನದ ಹೆಸರನ್ನು ನಮೂದಿಸಿ.¦ഉൽപ്പന്നത്തിന്റെ പേര് നൽകുക.¦பொருளின் பெயரை உள்ளிடவும்.
        Enter a valid price.¦చెల్లుబాటు అయ్యే ధర నమోదు చేయండి.¦सही कीमत दर्ज करें।¦ಸರಿಯಾದ ಬೆಲೆ ನಮೂದಿಸಿ.¦ശരിയായ വില നൽകുക.¦சரியான விலையை உள்ளிடவும்.
        Enter a valid stock quantity.¦చెల్లుబాటు అయ్యే స్టాక్ పరిమాణం నమోదు చేయండి.¦सही स्टॉक मात्रा दर्ज करें।¦ಸರಿಯಾದ ಸ್ಟಾಕ್ ಪ್ರಮಾಣ ನಮೂದಿಸಿ.¦ശരിയായ സ്റ്റോക്ക് അളവ് നൽകുക.¦சரியான இருப்பு அளவை உள்ளிடவும்.
        Could not publish product: sign in again and retry.¦ఉత్పత్తి ప్రచురించలేకపోయింది: మళ్లీ సైన్ ఇన్ చేసి ప్రయత్నించండి.¦उत्पाद प्रकाशित नहीं हुआ: फिर साइन इन करके प्रयास करें।¦ಉತ್ಪನ್ನ ಪ್ರಕಟವಾಗಲಿಲ್ಲ: ಮತ್ತೆ ಸೈನ್ ಇನ್ ಮಾಡಿ.¦ഉൽപ്പന്നം പ്രസിദ്ധീകരിക്കാനായില്ല: വീണ്ടും സൈൻ ഇൻ ചെയ്യുക.¦பொருளை வெளியிட முடியவில்லை: மீண்டும் உள்நுழையவும்.
        Could not publish product: Supabase denied the database write. Check the farmer profile and products table policies.¦ఉత్పత్తి ప్రచురించలేకపోయింది: Supabase అనుమతి నిరాకరించింది. రైతు ప్రొఫైల్, పాలసీలను తనిఖీ చేయండి.¦उत्पाद प्रकाशित नहीं हुआ: Supabase ने अनुमति नहीं दी। किसान प्रोफ़ाइल और टेबल नीति जाँचें।¦ಉತ್ಪನ್ನ ಪ್ರಕಟವಾಗಲಿಲ್ಲ: Supabase ಅನುಮತಿ ನಿರಾಕರಿಸಿದೆ. ರೈತ ಪ್ರೊಫೈಲ್ ಮತ್ತು ನೀತಿಗಳನ್ನು ಪರಿಶೀಲಿಸಿ.¦ഉൽപ്പന്നം പ്രസിദ്ധീകരിക്കാനായില്ല: Supabase അനുമതി നിഷേധിച്ചു. കർഷക പ്രൊഫൈലും നയങ്ങളും പരിശോധിക്കുക.¦பொருளை வெளியிட முடியவில்லை: Supabase அனுமதிக்கவில்லை. விவசாயி சுயவிவரம் மற்றும் கொள்கைகளைச் சரிபார்க்கவும்.
        Could not publish product. Check your connection and Supabase setup, then retry.¦ఉత్పత్తి ప్రచురించలేకపోయింది. కనెక్షన్, Supabase సెటప్ తనిఖీ చేసి మళ్లీ ప్రయత్నించండి.¦उत्पाद प्रकाशित नहीं हुआ। कनेक्शन और Supabase सेटअप जाँचकर फिर प्रयास करें।¦ಉತ್ಪನ್ನ ಪ್ರಕಟವಾಗಲಿಲ್ಲ. ಸಂಪರ್ಕ ಮತ್ತು Supabase ಸೆಟಪ್ ಪರಿಶೀಲಿಸಿ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.¦ഉൽപ്പന്നം പ്രസിദ്ധീകരിക്കാനായില്ല. കണക്ഷനും Supabase സജ്ജീകരണവും പരിശോധിച്ച് വീണ്ടും ശ്രമിക്കുക.¦பொருளை வெளியிட முடியவில்லை. இணைப்பு, Supabase அமைப்பைச் சரிபார்த்து மீண்டும் முயற்சிக்கவும்.
        Could not upload the photo: create a public Supabase Storage bucket named 'product-images', then retry.¦ఫోటో అప్‌లోడ్ కాలేదు: 'product-images' అనే Public Supabase బకెట్ సృష్టించి మళ్లీ ప్రయత్నించండి.¦तस्वीर अपलोड नहीं हुई: 'product-images' नाम का सार्वजनिक Supabase बकेट बनाएँ।¦ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಆಗಲಿಲ್ಲ: 'product-images' ಎಂಬ ಸಾರ್ವಜನಿಕ Supabase ಬಕೆಟ್ ರಚಿಸಿ.¦ചിത്രം അപ്‌ലോഡ് ചെയ്തില്ല: 'product-images' എന്ന പൊതു Supabase ബക്കറ്റ് സൃഷ്ടിക്കുക.¦படம் பதிவேறவில்லை: 'product-images' என்ற பொது Supabase பக்கெட்டை உருவாக்கவும்.
        Could not relist product¦ఉత్పత్తిని మళ్లీ జాబితా చేయలేకపోయింది¦उत्पाद फिर से सूचीबद्ध नहीं हुआ¦ಉತ್ಪನ್ನವನ್ನು ಮತ್ತೆ ಪಟ್ಟಿ ಮಾಡಲಾಗಲಿಲ್ಲ¦ഉൽപ്പന്നം വീണ്ടും ലിസ്റ്റ് ചെയ്യാനായില്ല¦பொருளை மீண்டும் பட்டியலிட முடியவில்லை
        Update failed¦నవీకరణ విఫలమైంది¦अपडेट विफल¦ನವೀಕರಣ ವಿಫಲವಾಗಿದೆ¦പുതുക്കൽ പരാജയപ്പെട്ടു¦புதுப்பிப்பு தோல்வியடைந்தது
        Mark sold out¦స్టాక్ అయిపోయింది అని గుర్తించండి¦स्टॉक ख़त्म चिह्नित करें¦ಸ್ಟಾಕ್ ಮುಗಿದಿದೆ ಎಂದು ಗುರುತಿಸಿ¦സ്റ്റോക്ക് തീർന്നതായി അടയാളപ്പെടുത്തുക¦கையிருப்பு தீர்ந்தது எனக் குறிக்கவும்
        ✅ Accept Order¦✅ ఆర్డర్‌ను ఆమోదించండి¦✅ ऑर्डर स्वीकार करें¦✅ ಆರ್ಡರ್ ಸ್ವೀಕರಿಸಿ¦✅ ഓർഡർ സ്വീകരിക്കുക¦✅ ஆர்டரை ஏற்கவும்
        📦 Mark Ready¦📦 సిద్ధంగా గుర్తించండి¦📦 तैयार चिह्नित करें¦📦 ಸಿದ್ಧವಾಗಿದೆ ಎಂದು ಗುರುತಿಸಿ¦📦 തയ്യാറായി അടയാളപ്പെടുത്തുക¦📦 தயாராக உள்ளது எனக் குறிக்கவும்
        🚚 Start Delivery¦🚚 డెలివరీ ప్రారంభించండి¦🚚 डिलीवरी शुरू करें¦🚚 ವಿತರಣೆ ಆರಂಭಿಸಿ¦🚚 ഡെലിവറി ആരംഭിക്കുക¦🚚 விநியோகத்தைத் தொடங்கு
        🎉 Mark Delivered¦🎉 డెలివరీ అయింది అని గుర్తించండి¦🎉 डिलीवर चिह्नित करें¦🎉 ವಿತರಿಸಲಾಗಿದೆ ಎಂದು ಗುರುತಿಸಿ¦🎉 ഡെലിവറി ചെയ്തു എന്ന് അടയാളപ്പെടുത്തുക¦🎉 விநியோகிக்கப்பட்டது எனக் குறிக்கவும்
        20 km marketplace radius active¦20 కి.మీ. మార్కెట్‌ప్లేస్ പരിധി చురుకుగా ఉంది¦20 किमी मार्केटप्लेस दायरा सक्रिय है¦20 ಕಿ.ಮೀ ಮಾರುಕಟ್ಟೆ ವ್ಯಾಪ್ತಿ ಸಕ್ರಿಯವಾಗಿದೆ¦20 കി.മീ മാർക്കറ്റ് പരിധി സജീവമാണ്¦20 கி.மீ. சந்தை வரம்பு செயல்பாட்டில் உள்ளது
        Account created. Confirm the link in your email, then sign in with your password.¦ఖాతా సృష్టించబడింది. ఇమెయిల్ లింక్ నిర్ధారించి పాస్‌వర్డ్‌తో సైన్ ఇన్ చేయండి.¦खाता बन गया। ईमेल लिंक की पुष्टि कर पासवर्ड से साइन इन करें।¦ಖಾತೆ ರಚಿಸಲಾಗಿದೆ. ಇಮೇಲ್ ಲಿಂಕ್ ದೃಢೀಕರಿಸಿ ಪಾಸ್‌ವರ್ಡ್‌ನೊಂದಿಗೆ ಸೈನ್ ಇನ್ ಮಾಡಿ.¦അക്കൗണ്ട് സൃഷ്ടിച്ചു. ഇമെയിൽ ലിങ്ക് സ്ഥിരീകരിച്ച് പാസ്‌വേഡ് ഉപയോഗിച്ച് സൈൻ ഇൻ ചെയ്യുക.¦கணக்கு உருவாக்கப்பட்டது. மின்னஞ்சல் இணைப்பை உறுதி செய்து கடவுச்சொல்லுடன் உள்நுழையவும்.
        Enter your email address.¦మీ ఇమెయిల్ చిరునామా నమోదు చేయండి.¦अपना ईमेल पता दर्ज करें।¦ನಿಮ್ಮ ಇಮೇಲ್ ವಿಳಾಸ ನಮೂದಿಸಿ.¦നിങ്ങളുടെ ഇമെയിൽ വിലാസം നൽകുക.¦உங்கள் மின்னஞ்சல் முகவரியை உள்ளிடவும்.
        Enter a valid email address.¦చెల్లుబాటు అయ్యే ఇమెయిల్ నమోదు చేయండి.¦सही ईमेल पता दर्ज करें।¦ಸರಿಯಾದ ಇಮೇಲ್ ವಿಳಾಸ ನಮೂದಿಸಿ.¦ശരിയായ ഇമെയിൽ വിലാസം നൽകുക.¦சரியான மின்னஞ்சல் முகவரியை உள்ளிடவும்.
        Enter your password.¦మీ పాస్‌వర్డ్ నమోదు చేయండి.¦अपना पासवर्ड दर्ज करें।¦ನಿಮ್ಮ ಪಾಸ್‌ವರ್ಡ್ ನಮೂದಿಸಿ.¦നിങ്ങളുടെ പാസ്‌വേഡ് നൽകുക.¦உங்கள் கடவுச்சொல்லை உள்ளிடவும்.
        Enter your full name to create an account.¦ఖాతా సృష్టించడానికి పూర్తి పేరు నమోదు చేయండి.¦खाता बनाने के लिए पूरा नाम दर्ज करें।¦ಖಾತೆ ರಚಿಸಲು ಪೂರ್ಣ ಹೆಸರು ನಮೂದಿಸಿ.¦അക്കൗണ്ട് സൃഷ്ടിക്കാൻ പൂർണ്ണ പേര് നൽകുക.¦கணக்கை உருவாக்க முழுப் பெயரை உள்ளிடவும்.
        Use a password with at least 8 characters.¦కనీసం 8 అక్షరాల పాస్‌వర్డ్ వాడండి.¦कम से कम 8 अक्षर का पासवर्ड रखें।¦ಕನಿಷ್ಠ 8 ಅಕ್ಷರಗಳ ಪಾಸ್‌ವರ್ಡ್ ಬಳಸಿ.¦കുറഞ്ഞത് 8 അക്ഷരമുള്ള പാസ്‌വേഡ് ഉപയോഗിക്കുക.¦குறைந்தது 8 எழுத்துகள் கொண்ட கடவுச்சொல்லைப் பயன்படுத்தவும்.
        Sign-in succeeded. Set your location, then continue to finish your profile.¦సైన్ ఇన్ అయింది. స్థానాన్ని సెట్ చేసి ప్రొఫైల్ పూర్తి చేయండి.¦साइन इन सफल। स्थान सेट करके प्रोफ़ाइल पूरी करें।¦ಸೈನ್ ಇನ್ ಯಶಸ್ವಿ. ಸ್ಥಳ ಹೊಂದಿಸಿ ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ.¦സൈൻ ഇൻ ചെയ്തു. ലൊക്കേഷൻ സജ്ജമാക്കി പ്രൊഫൈൽ പൂർത്തിയാക്കുക.¦உள்நுழைவு வெற்றி. இருப்பிடத்தை அமைத்து சுயவிவரத்தை முடிக்கவும்.
        Enter your name to continue in demo mode.¦డెమో మోడ్‌లో కొనసాగడానికి పేరు నమోదు చేయండి.¦डेमो मोड में आगे बढ़ने के लिए नाम लिखें।¦ಡೆಮೊ ಮೋಡ್ ಮುಂದುವರಿಸಲು ಹೆಸರು ನಮೂದಿಸಿ.¦ഡെമോ മോഡിൽ തുടരാൻ പേര് നൽകുക.¦செய்முறை நிலையில் தொடர உங்கள் பெயரை உள்ளிடவும்.
        Could not sign in. Check your connection and Supabase settings.¦సైన్ ఇన్ కాలేదు. కనెక్షన్, Supabase సెట్టింగ్‌లను తనిఖీ చేయండి.¦साइन इन नहीं हुआ। कनेक्शन और Supabase सेटिंग जाँचें।¦ಸೈನ್ ಇನ್ ಆಗಲಿಲ್ಲ. ಸಂಪರ್ಕ ಮತ್ತು Supabase ಸೆಟ್ಟಿಂಗ್ ಪರಿಶೀಲಿಸಿ.¦സൈൻ ഇൻ ചെയ്യാനായില്ല. കണക്ഷനും Supabase ക്രമീകരണങ്ങളും പരിശോധിക്കുക.¦உள்நுழைய முடியவில்லை. இணைப்பு, Supabase அமைப்புகளைச் சரிபார்க்கவும்.
        Set your location before placing an order¦ఆర్డర్ చేయడానికి ముందు స్థానాన్ని సెట్ చేయండి¦ऑर्डर करने से पहले स्थान सेट करें¦ಆರ್ಡರ್ ಮಾಡುವ ಮೊದಲು ಸ್ಥಳ ಹೊಂದಿಸಿ¦ഓർഡർ ചെയ്യുന്നതിന് മുമ്പ് ലൊക്കേഷൻ സജ്ജമാക്കുക¦ஆர்டர் செய்வதற்கு முன் இருப்பிடத்தை அமைக்கவும்
        Order placed 🎉¦ఆర్డర్ చేయబడింది 🎉¦ऑर्डर हो गया 🎉¦ಆರ್ಡರ್ ಮಾಡಲಾಗಿದೆ 🎉¦ഓർഡർ ചെയ്തു 🎉¦ஆர்டர் செய்யப்பட்டது 🎉
        Demo order placed 🎉¦డెమో ఆర్డర్ చేయబడింది 🎉¦डेमो ऑर्डर हो गया 🎉¦ಡೆಮೊ ಆರ್ಡರ್ ಮಾಡಲಾಗಿದೆ 🎉¦ഡെമോ ഓർഡർ ചെയ്തു 🎉¦செய்முறை ஆர்டர் செய்யப்பட்டது 🎉
        Order failed¦ఆర్డర్ విఫలమైంది¦ऑर्डर विफल हुआ¦ಆರ್ಡರ್ ವಿಫಲವಾಗಿದೆ¦ഓർഡർ പരാജയപ്പെട്ടു¦ஆர்டர் தோல்வியடைந்தது
        Could not sign out¦సైన్ అవుట్ కాలేదు¦साइन आउट नहीं हुआ¦ಸೈನ್ ಔಟ್ ಆಗಲಿಲ್ಲ¦സൈൻ ഔട്ട് ചെയ്യാനായില്ല¦வெளியேற முடியவில்லை
        Order #{id}¦ఆర్డర్ #{id}¦ऑर्डर #{id}¦ಆರ್ಡರ್ #{id}¦ഓർഡർ #{id}¦ஆர்டர் #{id}
        All¦అన్నీ¦सभी¦ಎಲ್ಲಾ¦എല്ലാം¦அனைத்தும்
        Pending¦పెండింగ్¦लंबित¦ಬಾಕಿ¦കാത്തിരിക്കുന്നു¦நிலுவையில்
        Active¦యాక్టివ్¦सक्रिय¦ಸಕ್ರಿಯ¦സജീവം¦செயலில்
        hidden¦దాచబడింది¦छिपा हुआ¦ಮರೆಮಾಡಲಾಗಿದೆ¦മറച്ചിരിക്കുന്നു¦மறைக்கப்பட்டது
        Placed¦ప్లేస్ చేయబడింది¦ऑर्डर किया गया¦ಆರ್ಡರ್ ಮಾಡಲಾಗಿದೆ¦ഓർഡർ ചെയ്തു¦ஆர்டர் செய்யப்பட்டது
        Preparing¦తయారవుతోంది¦तैयार हो रहा है¦ತಯಾರಾಗುತ್ತಿದೆ¦തയ്യാറാകുന്നു¦தயாராகிறது
        No {value} orders¦{value} ఆర్డర్లు లేవు¦कोई {value} ऑर्डर नहीं¦ಯಾವುದೇ {value} ಆರ್ಡರ್‌ಗಳಿಲ್ಲ¦{value} ഓർഡറുകളില്ല¦{value} ஆர்டர்கள் இல்லை
        A customer placed order #{id}.¦కస్టమర్ ఆర్డర్ #{id} చేశారు.¦ग्राहक ने ऑर्डर #{id} दिया।¦ಗ್ರಾಹಕರು ಆರ್ಡರ್ #{id} ಮಾಡಿದ್ದಾರೆ.¦ഒരു ഉപഭോക്താവ് ഓർഡർ #{id} നൽകി.¦வாடிக்கையாளர் ஆர்டர் #{id} செய்தார்.
        Order #{id} is {status}.¦ఆర్డర్ #{id}: {status}.¦ऑर्डर #{id} {status}।¦ಆರ್ಡರ್ #{id} {status}.¦ഓർഡർ #{id} {status}.¦ஆர்டர் #{id} {status}.
        Could not read location — ensure device GPS is on.¦స్థానాన్ని చదవలేకపోయాం — GPS ఆన్‌లో ఉందో చూడండి.¦स्थान नहीं मिला — डिवाइस GPS चालू करें।¦ಸ್ಥಳ ಓದಲಾಗಲಿಲ್ಲ — GPS ಆನ್ ಇದೆಯೇ ನೋಡಿ.¦ലൊക്കേഷൻ ലഭിച്ചില്ല — GPS ഓണാണെന്ന് ഉറപ്പാക്കുക.¦இருப்பிடத்தைப் படிக்க முடியவில்லை — GPS இயக்கப்பட்டுள்ளதா பார்க்கவும்.
        Location permission is required for the 20 km marketplace.¦20 కి.మీ. మార్కెట్‌ప్లేస్‌కు స్థాన అనుమతి అవసరం.¦20 किमी मार्केटप्लेस के लिए स्थान अनुमति ज़रूरी है।¦20 ಕಿ.ಮೀ ಮಾರುಕಟ್ಟೆಗೆ ಸ್ಥಳ ಅನುಮತಿ ಅಗತ್ಯ.¦20 കി.മീ മാർക്കറ്റിന് ലൊക്കേഷൻ അനുമതി ആവശ്യമാണ്.¦20 கி.மீ. சந்தைக்கு இருப்பிட அனுமதி தேவை.
    """.trimIndent().lines()

    for (row in rows) {
        val columns = row.split('¦').map { it.replace("\\n", "\n") }
        if (columns.size == 6) {
            for ((index, language) in translationColumns.withIndex()) {
                val existing = result.getOrPut(language) { mutableMapOf() }
                existing[columns[0]] = columns[index + 1]
            }
        }
    }
    result
}

fun localizeAppText(text: String, language: String): String {
    if (language == "en") return text
    val dictionary = translations[language] ?: return text
    dictionary[text]?.let { return it }

    fun template(key: String, vararg replacements: Pair<String, String>): String? =
        dictionary[key]?.let { value -> replacements.fold(value) { result, pair -> result.replace(pair.first, pair.second) } }

    Regex("^Seller: (.+)$").matchEntire(text)?.let { return template("Seller: {name}", "{name}" to it.groupValues[1]) ?: text }
    Regex("^(\\d+) items? in cart$").matchEntire(text)?.let { return template("{count} item(s) in cart", "{count}" to it.groupValues[1]) ?: text }
    Regex("^(\\d+) products? available$").matchEntire(text)?.let { return template("{count} product(s) available", "{count}" to it.groupValues[1]) ?: text }
    Regex("^(\\d+) new orders? waiting$").matchEntire(text)?.let { return template("{count} new order(s) waiting", "{count}" to it.groupValues[1]) ?: text }
    Regex("^View all (\\d+) orders →$").matchEntire(text)?.let { return template("View all {count} orders →", "{count}" to it.groupValues[1]) ?: text }
    Regex("^Items \\((\\d+)\\)$").matchEntire(text)?.let { return template("Items ({count})", "{count}" to it.groupValues[1]) ?: text }
    Regex("^(\\d+) kg available$").matchEntire(text)?.let { return template("{count} kg available", "{count}" to it.groupValues[1]) ?: text }
    Regex("^Delivery address: (.+)$").matchEntire(text)?.let { return template("Delivery address: {value}", "{value}" to it.groupValues[1]) ?: text }
    Regex("^Customer contact: (.+)$").matchEntire(text)?.let { return template("Customer contact: {value}", "{value}" to it.groupValues[1]) ?: text }
    Regex("^Customer: (.+)$").matchEntire(text)?.let { return template("Customer: {value}", "{value}" to it.groupValues[1]) ?: text }
    Regex("^Estimated delivery by (.+) \\(within 24 hours\\)$").matchEntire(text)?.let { return template("Estimated delivery by {value} (within 24 hours)", "{value}" to it.groupValues[1]) ?: text }
    Regex("^(.+) added to cart 🛒$").matchEntire(text)?.let { return template("{name} added to cart 🛒", "{name}" to it.groupValues[1]) ?: text }
    Regex("^Order #([A-Fa-f0-9]+)$").matchEntire(text)?.let { return template("Order #{id}", "{id}" to it.groupValues[1]) ?: text }
    Regex("^A customer placed order #([A-Fa-f0-9]+)\\.$").matchEntire(text)?.let {
        return template("A customer placed order #{id}.", "{id}" to it.groupValues[1]) ?: text
    }
    Regex("^No (.+) orders$").matchEntire(text)?.let {
        val section = localizeAppText(it.groupValues[1], language)
        return template("No {value} orders", "{value}" to section) ?: text
    }
    Regex("^Order #([A-Fa-f0-9]+) is (.+)\\.$").matchEntire(text)?.let {
        val status = localizeAppText(it.groupValues[2].replace('_', ' '), language)
        return template("Order #{id} is {status}.", "{id}" to it.groupValues[1], "{status}" to status) ?: text
    }
    Regex("^(🥕|🍎|🥬|🌾) (Vegetables|Fruits|Leafy Greens|Grains)$").matchEntire(text)?.let {
        return "${it.groupValues[1]} ${dictionary[it.groupValues[2]] ?: it.groupValues[2]}"
    }
    Regex("^(Vegetables|Fruits|Leafy Greens|Grains) · (.+)$").matchEntire(text)?.let {
        return "${dictionary[it.groupValues[1]] ?: it.groupValues[1]} · ${it.groupValues[2]}"
    }
    return text
}
