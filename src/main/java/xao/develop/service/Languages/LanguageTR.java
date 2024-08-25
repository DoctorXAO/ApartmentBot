package xao.develop.service.Languages;

public class LanguageTR implements Language {
    @Override
    public String getStart() {
        return """
            <b>Hoş geldiniz!</b>
            
            Botumuza hoş geldiniz! 🌴
            
            Güneşli Alanya'da mükemmel bir konut mu arıyorsunuz? \s
            Plaja sadece birkaç adım uzaklıktaki evimiz, Türkiye'nin en iyi \
            tatil yerlerinden birinde size konfor ve rahatlık sunuyor. Evimiz beş katlıdır \
            ve konforlu bir yaşam için ihtiyacınız olan her şeye sahiptir.
            
            Menüden ilginizi çeken bölümü seçin, hayalinizdeki daireyi bulmanıza yardımcı olalım!
            """;
    }

    @Override
    public String getHouseInformation() {
        return """
            🏠 <b>Kat Sayısı:</b> 5 kat
            
            🕖 <b>Çalışma Saatleri:</b> 24/7
            
            🗺 <b>Adres:</b> Alanya, Oba, 17.nci sokak No:9, 07460
            """;
    }

    @Override
    public String getContacts(String phone, String email) {
        return String.format("""
            <b>İletişim:</b>
            
            📞 <b>Telefon (Telegram, WhatsApp)</b>:
            %s
            
            ✉️ <b>E-posta:</b> %s
            
            Bizimle telefon veya e-posta yoluyla iletişime geçebilirsiniz. \
            Herhangi bir sorunuzda size yardımcı olmaktan memnuniyet duyarız!
            """, phone, email);
    }

    @Override
    public String getRules() {
        return """
            <b>Ev Kuralları:</b>
            
            1. 🕒 <b>Giriş/Çıkış:</b> Giriş saati 14:00'ten itibaren, çıkış saati ise en geç 11:00'dir.
            
            2. 🚭 <b>Sigara:</b> Dairelerde ve ortak alanlarda sigara içmek yasaktır. \
            Sigara sadece belirlenmiş alanlarda içilebilir.
            
            3. 🧹 <b>Temizlik:</b> Dairelerde ve bina içinde temizliği lütfen koruyun. \
            Çöpleri ayırın ve uygun konteynerlere atın.
            
            4. 📢 <b>Sessizlik:</b> Diğer sakinleri rahatsız etmemek için 22:00-08:00 saatleri \
            arasında sessizliği korumanızı rica ederiz.
            
            5. 🐾 <b>Evcil Hayvanlar:</b> Evcil hayvanlarla konaklama yalnızca önceden onay alınarak mümkündür.
            
            6. 🛠 <b>Arızaların Bildirilmesi:</b> Herhangi bir arıza veya hasar tespit ederseniz, \
            lütfen derhal yönetime bildirin.
            
            Bu basit kurallara uyarak, kendiniz ve diğer sakinler için konforlu ve keyifli \
            bir konaklama sağlayabilirsiniz!
            """;
    }

    @Override
    public String getApartments() {
        return """
            <b>Daire Hakkında Bilgi:</b>
            
            Evimiz, her kata kolay erişim sağlayan bir asansörle donatılmış beş katlı bir binadır.
            
            🏢 <b>Genel Açıklama:</b> Her katta 4 daire ve banyo malzemelerinin bulunduğu iki depo alanı vardır.
            
            🛏 <b>Olanaklar:</b>
            - Modern dekorasyon ve kaliteli malzemeler
            - Klima
            - Elektrikli ocak
            - Buzdolabı
            - Bulaşık makinesi
            - Elektrikli çaydanlık
            - Mutfak takımı* (5 bardak, 3 yemek kaşığı, 3 çatal, 3 çay kaşığı,\s
             3 kase, 1 büyük tabak, 1 derin kase, 2 tencere (büyük ve küçük),\s
             1 tava)
            - Ütü masası
            - Ütü
            - Çamaşır makinesi
            - Duş
            - Tuvalet
            
            Evinizdeki konfor ve güvenliği keyifle yaşayın! Evimiz, sizin rahatlığınız \
            ve hoş bir yaşam sürmeniz için tasarlandı.
            
            * Mutfak takımı daireden daireye farklılık gösterebilir, ancak genel \
            olarak belirtilen listeye uygun olacaktır.
            """;
    }

    @Override
    public String getRentAnApartment() {
        return """
            <b>Kiralama Süreci:</b>
            
            Evimizde bir daire kiralamak için lütfen aşağıdaki adımları izleyin:
            
            1. 📋 <b>Başvuru Formunu Doldurun:</b> Adınızı, iletişim bilgilerinizi ve \
            tercih ettiğiniz taşınma tarihini belirtin. Detayları netleştirmek için \
            sizinle iletişime geçeceğiz.
            
            2. 💬 <b>Şartları Görüşün:</b> Acentemiz, kira şartlarını görüşmek \
            üzere sizinle iletişime geçecektir.
            
            3. 📝 <b>Sözleşmeyi İmzalama:</b> Kira sözleşmesini hazırlayıp imzalamanız için size göndereceğiz. \
            Ayrıca merak ettiğiniz soruları sorabilirsiniz.
            
            4. 🔑 <b>Taşınma:</b> Sözleşmeyi imzaladıktan ve ödeme yaptıktan sonra yeni dairenizin \
            anahtarlarını alacak ve taşınabileceksiniz.
            
            Kiralama sürecinin herhangi bir aşamasında sorularınız olursa lütfen \
            bizimle iletişime geçin. Size yardımcı olmaktan her zaman memnuniyet duyarız!
            """;
    }

    @Override
    public String getChangeLanguage() {
        return """
            <b>Yerelleştirme Seçimi</b>
            
            🏁 Lütfen kullanımı sizin için uygun olan dili seçiniz!
            """;
    }

    @Override
    public String getButtonBack() {
        return "⬅️ Geri";
    }

    @Override
    public String getButtonApartments() {
        return "\uD83C\uDFE0 Daireler";
    }

    @Override
    public String getButtonRentAnApartment() {
        return "\uD83D\uDD11 Daire kiralamak";
    }

    @Override
    public String getButtonHouseInformation() {
        return "ℹ️ Ev hakkında";
    }

    @Override
    public String getButtonContacts() {
        return "\uD83D\uDCDE İletişim";
    }

    @Override
    public String getButtonChangeLanguage() {
        return "\uD83C\uDDF9\uD83C\uDDF7 Türkçe";
    }

    @Override
    public String getButtonRules() {
        return "\uD83D\uDEAD Konaklama kuralları";
    }

    @Override
    public String getButtonFillOutAnApplication() {
        return "\uD83D\uDD8A Başvuru formunu doldurmak";
    }
}
