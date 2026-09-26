package com.kitappla.app.ui.nav

import com.kitappla.app.core.session.SessionState

/**
 * Sallama yalnızca oturum kapalıyken (Guest) ve Yönetici Girişi zaten açık değilken dinlenir; Yükleniyor/Üye/Yönetici
 * durumlarında asla. Rota henüz bilinmiyorsa (null) misafir için dinlenir.
 */
fun shakeGateEnabled(session: SessionState, currentRoute: String?): Boolean =
    session is SessionState.Guest && currentRoute != Routes.ADMIN_LOGIN
