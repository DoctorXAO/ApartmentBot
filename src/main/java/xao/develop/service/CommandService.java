package xao.develop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xao.develop.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

public class CommandService implements BotService {

    @Autowired
    private BotConfig botConfig;

    private final String TEXT_START = """
                    <b>Добро пожаловать!</b>
                    
                    Рады приветствовать Вас в нашем боте! \uD83C\uDF34
                    
                    Вы ищете идеальное жилье в солнечной Алании?\
                    Наш дом, расположенный всего в нескольких шагах от пляжа, \
                    предлагает Вам комфорт и удобства в одном из лучших курортных мест Турции. \
                    В нашем доме пять этажей и все, что нужно для комфортного проживания.
                    
                    Выберите интересующий Вас раздел в меню, и мы поможем вам найти квартиру вашей мечты!""";

    private final String TEXT_HOUSE_INFORMATION = """
            🏠 <b>Этажность:</b> 5 этажей
            
            🕖 <b>Часы работы:</b> 24/7
            
            🗺 <b>Адрес:</b> Alanya, Oba, 17.nci sokak No:9, 07460
            """;

    private final String TEXT_CONTACTS = """
            <b>Контакты:</b>
            
            📞 <b>Телефон (telegram, whatsapp):</b> +90 530 293 52 71 - Ali
            ✉️ <b>Email:</b> setemail@noemail.com
            
            Вы можете связаться с нами по телефону или электронной почте. \
            Мы всегда рады помочь Вам с любыми вопросами!
            """;

    private final String TEXT_RULES = """
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

    private final String TEXT_APARTMENTS = """
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

    private final String TEXT_RENT_AN_APARTMENT = """
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

    @Override
    public Message cmd_start(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_START,
                getMainIKMarkup()));
    }

    @Override
    public Message data_apartments(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_APARTMENTS,
                getApartmentsIKMarkup()));
    }

    @Override
    public Message data_rent_an_apartment(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_RENT_AN_APARTMENT,
                getRentAnApartmentIKMarkup()));
    }

    @Override
    public Message data_house_information(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_HOUSE_INFORMATION,
                getHouseInformationIKMarkup()));
    }

    @Override
    public Message data_contacts(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_CONTACTS,
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(buildIKButton("⬅️ Назад", "back_from_contacts")))
                        .build()));
    }

    @Override
    public Message data_rules(Update update) throws TelegramApiException {
        return botConfig.getTelegramClient().execute(buildSendMessage(update,
                TEXT_RULES,
                InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(new InlineKeyboardRow(buildIKButton("⬅️ Назад", "back_from_rules")))
                        .build()));
    }

    @Override
    public Message dataIsNotRecognized() {
        return null;
    }

    private InlineKeyboardMarkup getMainIKMarkup() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
                buttons.add(buildIKButton("\uD83C\uDFE0 Квартиры", "available_apartments"));
        buttons.add(buildIKButton("\uD83D\uDD11 Оформить аренду", "rent_an_apartment"));
        InlineKeyboardRow row1 = buildIKRow(buttons);

        buttons.clear();
        buttons.add(buildIKButton("ℹ️ О доме", "house_information"));
        buttons.add(buildIKButton("\uD83D\uDCDE Контакты", "contacts"));
        InlineKeyboardRow row2 = buildIKRow(buttons);

        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .build();
    }

    private InlineKeyboardMarkup getHouseInformationIKMarkup() {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83D\uDEAD Правила проживания", "rules")))
                .keyboardRow(new InlineKeyboardRow(buildIKButton("⬅️ Назад", "back_from_house_information")))
                .build();
    }

    private InlineKeyboardMarkup getApartmentsIKMarkup() {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton("⬅️ Назад", "back_from_apartments")))
                .build();
    }

    private InlineKeyboardMarkup getRentAnApartmentIKMarkup() {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buildIKButton("\uD83D\uDD8A Заполнить заявку", "fill_out_an_application")))
                .keyboardRow(new InlineKeyboardRow(buildIKButton("⬅️ Назад", "back_from_rent_an_apartment")))
                .build();
    }

    private InlineKeyboardRow buildIKRow(List<InlineKeyboardButton> buttons) {
        return new InlineKeyboardRow(buttons);
    }

    private InlineKeyboardButton buildIKButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private SendMessage buildSendMessage(Update update, String text, InlineKeyboardMarkup markup) {
        long chatID;
        if (update.hasMessage())
            chatID = update.getMessage().getChatId();
        else if (update.hasCallbackQuery())
            chatID = update.getCallbackQuery().getMessage().getChatId();
        else
            chatID = 0;

        return SendMessage
                .builder()
                .chatId(chatID)
                .text(text)
                .replyMarkup(markup)
                .parseMode("HTML")
                .build();
    }
}
