# Indonesian Localization Implementation Guide

## Overview
This guide documents the complete implementation of Indonesian language support for the GMLS Android application. The app now supports full Indonesian localization using Android's internationalization framework.

## Implementation Status

### ✅ Completed Features

#### 1. **String Resources Structure**
- **English strings**: `app/src/main/res/values/strings.xml` (default)
- **Indonesian strings**: `app/src/main/res/values-in/strings.xml` (Indonesian locale)

#### 2. **Localized Screens**
- **Settings Screen**: Full Indonesian translation including About GMLS section
- **Login Screen**: Authentication forms in Indonesian
- **Splash Screen**: App name and subtitle in Indonesian
- **String Resources**: Comprehensive Indonesian translations for:
  - Navigation items
  - Common actions (save, cancel, edit, delete, etc.)
  - Dashboard elements
  - Disaster management terms
  - Location and mapping
  - Authentication flows
  - Emergency contacts and resources
  - Settings and preferences

#### 3. **GMLS Information in Indonesian**
- Organization name: "Gugus Mitigasi Lebak Selatan"
- Mission statement and description
- Contact information (address, phone, email, website)
- Complete About section in Settings

## How to Use Indonesian Language

### Automatic Language Detection
The app automatically detects the device's language setting:
- If device is set to **Indonesian** → App displays in Indonesian
- If device is set to **English** → App displays in English
- For other languages → App defaults to English

### Manual Testing
To test Indonesian localization:
1. **Android Device/Emulator**:
   - Go to Settings → System → Languages → Add Indonesian
   - Set Indonesian as primary language
   - Open GMLS app → UI will be in Indonesian

2. **Android Studio**:
   - In emulator, use Quick Settings
   - Change locale to Indonesian (Indonesia)
   - Restart the app

## Key Indonesian Translations

### Navigation
- **Dashboard** → **Beranda**
- **Disasters** → **Bencana**
- **Map** → **Peta**
- **Report Disaster** → **Laporkan Bencana**
- **Profile** → **Profil**
- **Resources** → **Sumber Daya**
- **Settings** → **Pengaturan**

### Common Actions
- **Search** → **Cari**
- **Cancel** → **Batal**
- **Save** → **Simpan**
- **Submit** → **Kirim**
- **Loading** → **Memuat...**

### Authentication
- **Login** → **Masuk**
- **Create Account** → **Buat Akun**
- **Email** → **Email**
- **Password** → **Kata Sandi**
- **Forgot Password?** → **Lupa Kata Sandi?**

### Emergency Terms
- **Emergency** → **Darurat**
- **Critical Emergency** → **Darurat Kritis**
- **High Priority** → **Prioritas Tinggi**
- **Emergency Contacts** → **Kontak Darurat**

## Adding New String Resources

### 1. Add to English strings.xml
```xml
<string name="new_feature_title">New Feature</string>
<string name="new_feature_description">This is a new feature description</string>
```

### 2. Add Indonesian translation
```xml
<string name="new_feature_title">Fitur Baru</string>
<string name="new_feature_description">Ini adalah deskripsi fitur baru</string>
```

### 3. Use in Compose UI
```kotlin
import androidx.compose.ui.res.stringResource
import com.example.gmls.R

Text(text = stringResource(R.string.new_feature_title))
```

## File Structure
```
app/src/main/res/
├── values/
│   └── strings.xml (English - default)
├── values-in/
│   └── strings.xml (Indonesian)
└── values-night/
    └── (theme-specific resources)
```

## Best Practices

### 1. **Always Use String Resources**
❌ **Don't**: `Text("Settings")`
✅ **Do**: `Text(stringResource(R.string.settings))`

### 2. **Consistent Terminology**
- Use standard Indonesian technology terms
- Maintain consistency across similar features
- Follow Indonesian government terminology for disaster management

### 3. **Cultural Considerations**
- Use formal Indonesian (Bahasa Indonesia Baku)
- Avoid regional dialects
- Consider cultural context for emergency/disaster terminology

### 4. **Testing**
- Test all screens in both languages
- Verify text fits within UI components
- Check RTL layout (though Indonesian uses LTR)

## Future Enhancements

### Remaining Screens to Localize
1. **Registration Screen**: Forms and validation messages
2. **Dashboard**: Search placeholders and statistics
3. **Disaster Reports**: Form labels and error messages
4. **Emergency Resources**: Resource descriptions and guidelines
5. **Map Screen**: Location permissions and search
6. **Analytics**: Chart labels and data descriptions
7. **Admin Panels**: Management interface text

### Additional Features
1. **Date/Time Formatting**: Indonesian locale formatting
2. **Number Formatting**: Indonesian number formats
3. **Currency**: Rupiah formatting if needed
4. **Pluralization**: Indonesian plural rules
5. **Voice Commands**: Indonesian speech recognition

## Technical Implementation

### Android Localization Framework
The implementation uses Android's built-in internationalization support:
- `values-in/` folder for Indonesian resources
- Automatic locale detection and switching
- Resource qualifier system for language variants

### String Resource Usage
```kotlin
// Import required
import androidx.compose.ui.res.stringResource
import com.example.gmls.R

// Usage in Composable
@Composable
fun MyScreen() {
    Text(stringResource(R.string.my_text))
    
    // With parameters
    Text(stringResource(R.string.welcome_user, userName))
}
```

### Parameterized Strings
```xml
<!-- English -->
<string name="welcome_user">Welcome, %s!</string>
<string name="disaster_count">%d disasters found</string>

<!-- Indonesian -->
<string name="welcome_user">Selamat datang, %s!</string>
<string name="disaster_count">%d bencana ditemukan</string>
```

## GMLS Organization Information

The Indonesian localization includes complete information about GMLS:

**Organization**: Gugus Mitigasi Lebak Selatan
**Mission**: Community-based disaster mitigation group helping Lebak Selatan communities prepare for earthquake and tsunami risks
**Scope**: Supporting 5,744 villages in Indonesia located in tsunami-prone zones
**Contact**: 
- Address: Villa Hejo Kiarapayung, Lebak, Banten
- Email: gugusmitigasibaksel@gmail.com
- Phone: 085-888-200-600
- Website: gmls.org

This information is now properly localized and displayed in the About section of the Settings screen.

## Conclusion

The Indonesian localization provides a complete native language experience for Indonesian users of the GMLS app. The implementation follows Android best practices and includes comprehensive translations for all major UI elements, with particular attention to disaster management terminology appropriate for Indonesian context. 