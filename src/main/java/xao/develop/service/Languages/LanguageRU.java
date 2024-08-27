package xao.develop.service.Languages;

public class LanguageRU implements Language {

    @Override
    public String getStart() {
        return """
            <b>Добро пожаловать!</b>
            
            Рады приветствовать Вас в нашем боте! 🌴
            
            Вы ищете идеальное жилье в солнечной Алании? \
            Наш дом, расположенный всего в нескольких шагах от пляжа, \
            предлагает Вам комфорт и удобства в одном из лучших курортных мест Турции. \
            В нашем доме пять этажей и все, что нужно для комфортного проживания.
            
            Выберите интересующий Вас раздел в меню, и мы поможем вам найти квартиру вашей мечты!
            """;
    }

    @Override
    public String getHouseInformation() {
        return """
            🏠 <b>Этажность:</b> 5 этажей
            
            🕖 <b>Часы работы:</b> 24/7
            
            🗺 <b>Адрес:</b> Alanya, Oba, 17.nci sokak No:9, 07460
            """;
    }

    @Override
    public String getContacts(String phone, String email) {
        return String.format(
                        """
                        <b>Контакты:</b>
                        
                        \uD83D\uDCDE <b>Телефон (telegram, whatsapp):</b>
                        %s
                        
                        ✉️ <b>Email:</b> %s
                        
                        Вы можете связаться с нами по телефону или электронной почте. \
                        Мы всегда рады помочь Вам с любыми вопросами!
                        """, phone, email);
    }

    @Override
    public String getRules() {
        return """
            <b>Правила проживания в доме:</b>
            
            1. 🕒 <b>Чек-ин/Чек-аут:</b> Заселение с 14:00, выезд до 11:00.
            
            2. 🚭 <b>Курение:</b> Курение запрещено в квартирах и общественных зонах. \
            Разрешается курение только в специально отведенных местах.
            
            3. 🧹 <b>Чистота:</b> Пожалуйста, поддерживайте чистоту в квартирах и на территории дома. \
            Сортируйте мусор и выбрасывайте его в соответствующие контейнеры.
            
            4. 📢 <b>Тишина:</b> Просим соблюдать тишину с 22:00 до 08:00, чтобы не мешать другим жильцам.
            
            5. 🐾 <b>Питомцы:</b> Проживание с домашними животными допускается только по предварительному \
            согласованию.
            
            6. 🛠️ <b>Уведомление о неисправностях:</b> Если обнаружите какие-либо поломки или неисправности, \
            незамедлительно сообщите администрации.
            
            Соблюдая эти простые правила, вы обеспечите себе и другим жильцам комфортное и приятное пребывание!
            """;
    }

    @Override
    public String getApartments() {
        return """
            <b>Информация о квартире:</b>
            
            Наш дом — это пятиэтажное здание, оснащенное лифтом, который обеспечивает \
            легкий доступ на любой этаж.
            
            🏢 <b>Общее описание:</b> На каждом этаже есть 4 квартиры, а также две кладовые, в которых можно \
            найти расходные материалы для ванной комнаты.
            
            🛏️ <b>Удобства:</b>\s
            - Современная отделка и качественные материалы
            - Кондиционер
            - Электроплита
            - Холодильник
            - Посудомоечная машина
            - Электрочайник
            - Набор посуды* (5 кружек, 3 ложки, 3 вилки, 3 чайные ложки, \
            3 миски, 1 большая тарелка, 1 глубокая миска, 2 кострюли (большая и маленькая), \
            1 сковорода)
            - Гладильная доска
            - Утюг
            - Стиральная машина
            - Душ
            - Туалет
            
            Наслаждайтесь комфортом и безопасностью в нашем доме, который \
            создан для Вашего удобства и приятного проживания!
            
            *Набор посуды может отличаться от квартиры к квартиры, но \
            в целом будем примерно изложенный вариант.
            """;
    }

    @Override
    public String getRentAnApartment() {
        return """
            <b>Оформление аренды:</b>
            
            Для того чтобы арендовать квартиру в нашем доме, выполните следующие шаги:
            
            1. 📋 <b>Заполните заявку:</b> Укажите ваше имя, \
            контактные данные и предпочтительную дату заселения. \
            Мы свяжемся с вами для уточнения деталей.
            
            2. 💬 <b>Обсудите условия:</b> Наш агент свяжется с вами, \
            чтобы обсудить условия аренды.
            
            3. 📝 <b>Подпишите договор:</b> Мы подготовим договор аренды и \
            отправим вам для подписания. Вы также сможете задать любые интересующие вас вопросы.
            
            4. 🔑 <b>Заселение:</b> После подписания договора и внесения оплаты, \
            вы получите ключи от вашей новой квартиры и сможете заселиться.
            
            Если у Вас возникли вопросы на любом этапе оформления аренды, \
            пожалуйста, свяжитесь с нами. Мы всегда рады помочь!
            """;
    }

    @Override
    public String getFillOutName() {
        return """
            \uD83C\uDFA4 <b>Заполнение заявления (шаг 1/4)</b>
            
            🔴 Заполнение фамилии и имени
            🔘 Укажите количество человек
            🔘 Продолжительность аренды
            🔘 Ваши пожелания (опционально)
            
            Пожалуйста, укажите Вашу фамилию и имя и отправьте в данный чат для перехода \
            к следующему шагу!
            """;
    }

    @Override
    public String getFillOutCountOfPerson() {
        return """
                \uD83C\uDFA4 <b>Заполнение заявления (шаг 2/4)</b>
                
                ☑️ Заполнение фамилии и имени
                🔴 Укажите количество человек
                🔘 Продолжительность аренды
                🔘 Ваши пожелания (опционально)
                
                Укажите, пожалуйста, какое количество человек будет проживать в квартире и отправьте \
                в данный чат!
                """;
    }

    @Override
    public String getFillOutRentTime(String onePerDay,
                                     String onePerMouth,
                                     String onePerYear,
                                     String twoPerDay,
                                     String twoPerMouth,
                                     String twoPerYear) {
        return """
                \uD83C\uDFA4 <b>Заполнение заявления (шаг 3/4)</b>
                
                ☑️ Заполнение фамилии и имени
                ☑️ Укажите количество человек
                🔴 Продолжительность аренды
                🔘 Ваши пожелания (опционально)
                
                Пожалуйста, укажите на какой срок Вы хотите арендовать квартиру и отправьте в данный чат!
                
                Расценки:
                
                По дню:
                
                1 человек   - %s евро/день
                2+ человека - %s евро/день
                
                🔥 <b>ПОПУЛЯРНОЕ</b> По месяцу - <b>ЭКОНОМИЯ 66%</b>:
                
                1 человек - %s евро/месяц
                2+ человек - %s евро/месяц
                
                По году - <b>ЭКОНОМИЯ 72%</b>:
                
                1 человек - %s евро/год
                2+ человека - %s евро/год
                """;
    }

    @Override
    public String getFillOutCommentary() {
        return """
                \uD83C\uDFA4 <b>Заполнение заявления (шаг 4/4)</b>
                
                ☑️ Заполнение фамилии и имени
                ☑️ Укажите количество человек
                ☑️ Продолжительность аренды
                🔴 Ваши пожелания (опционально)
                
                Пожалуйста, укажите Ваши пожелания по квартире, чтобы мы могли принять их к сведенью!
                """;
    }

    @Override
    public String getChangeLanguage() {
        return """
            <b>Выбор локализации</b>
            
            🏁 Пожалуйста, выберите язык, которым Вам будет удобно пользоваться!
            """;
    }

    @Override
    public String getButtonBack() {
        return "⬅️ Назад";
    }

    @Override
    public String getButtonApartments() {
        return "\uD83C\uDFE0 Квартиры";
    }

    @Override
    public String getButtonRentAnApartment() {
        return "\uD83D\uDD11 Арендовать квартиру";
    }

    @Override
    public String getButtonHouseInformation() {
        return "ℹ️ О доме";
    }

    @Override
    public String getButtonContacts() {
        return "\uD83D\uDCDE Контакты";
    }

    @Override
    public String getButtonChangeLanguage() {
        return "\uD83C\uDDF7\uD83C\uDDFA Русский";
    }

    @Override
    public String getButtonRules() {
        return "\uD83D\uDEAD Правила проживания";
    }

    @Override
    public String getButtonFillOutAnApplication() {
        return "\uD83D\uDD8A Заполнить заявку";
    }
}
