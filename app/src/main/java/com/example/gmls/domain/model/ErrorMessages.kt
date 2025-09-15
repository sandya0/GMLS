package com.example.gmls.domain.model

object ErrorMessages {
    // Email validation errors
    const val EMAIL_REQUIRED = "Email wajib diisi"
    const val EMAIL_TOO_SHORT = "Email minimal 5 karakter"
    const val EMAIL_TOO_LONG = "Email maksimal 254 karakter"
    const val EMAIL_INVALID = "Masukkan alamat email yang valid"
    
    // Password validation errors
    const val PASSWORD_REQUIRED = "Kata sandi wajib diisi"
    const val PASSWORD_TOO_SHORT = "Kata sandi minimal 8 karakter"
    const val PASSWORD_TOO_LONG = "Kata sandi maksimal 128 karakter"
    const val PASSWORD_WEAK = "Kata sandi harus mengandung setidaknya satu huruf besar, satu huruf kecil, satu angka, dan satu karakter khusus"
    
    // Name validation errors
    const val NAME_REQUIRED = "Nama lengkap wajib diisi"
    const val NAME_TOO_SHORT = "Nama minimal 2 karakter"
    const val NAME_TOO_LONG = "Nama maksimal 100 karakter"
    const val NAME_INVALID = "Nama hanya boleh berisi huruf, spasi, tanda hubung, dan apostrof"
    
    // Phone validation errors
    const val PHONE_INVALID = "Masukkan nomor telepon yang valid"
    const val PHONE_TOO_SHORT = "Nomor telepon minimal 10 digit"
    const val PHONE_TOO_LONG = "Nomor telepon maksimal 15 digit"
    
    // Address validation errors
    const val ADDRESS_REQUIRED = "Alamat wajib diisi"
    const val ADDRESS_TOO_SHORT = "Alamat minimal 10 karakter"
    const val ADDRESS_TOO_LONG = "Alamat maksimal 500 karakter"
    
    // Search validation errors
    const val SEARCH_QUERY_TOO_LONG = "Pencarian maksimal 100 karakter"
    const val SEARCH_QUERY_INVALID = "Pencarian mengandung karakter tidak valid"
    
    // User ID validation errors
    const val USER_ID_INVALID = "Format ID pengguna tidak valid"
    const val USER_ID_TOO_SHORT = "ID pengguna minimal 10 karakter"
    const val USER_ID_TOO_LONG = "ID pengguna maksimal 50 karakter"
    
    // Admin operation errors
    const val ADMIN_PERMISSION_DENIED = "Diperlukan hak admin untuk operasi ini"
    const val USER_NOT_FOUND = "Pengguna tidak ditemukan"
    const val OPERATION_FAILED = "Operasi gagal. Silakan coba lagi."
    const val NETWORK_ERROR = "Kesalahan jaringan. Periksa koneksi Anda."
    const val CANNOT_DELETE_SELF = "Tidak dapat menghapus akun Anda sendiri"
    const val CANNOT_DEACTIVATE_SELF = "Tidak dapat menonaktifkan akun Anda sendiri"
    
    // General errors
    const val INVALID_INPUT = "Input tidak valid"
    const val FIELD_REQUIRED = "Kolom ini wajib diisi"
    const val OPERATION_NOT_ALLOWED = "Operasi tidak diizinkan"
} 
