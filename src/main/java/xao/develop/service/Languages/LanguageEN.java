package xao.develop.service.Languages;

public class LanguageEN implements Language {

    @Override
    public String getStart() {
        return """
            <b>Welcome!</b>
            
            We are glad to welcome you to our bot! 🌴
            
            Are you looking for the perfect accommodation in sunny Alanya? \
            Our house, located just a few steps from the beach, offers \
            you comfort and convenience in one of the best resort destinations in Turkey. \
            Our house has five floors and everything you need for a comfortable stay.
            
            Select the section you are interested in from the menu and we will \
            help you find the apartment of your dreams!
            """;
    }

    @Override
    public String getHouseInformation() {
        return """
            🏠 <b>Number of Floors:</b> 5 floors
            
            🕖 <b>Operating Hours:</b> 24/7
            
            🗺 <b>Address:</b> Alanya, Oba, 17.nci sokak No:9, 07460
            """;
    }

    @Override
    public String getContacts(String phone, String email) {
        return String.format("""
            <b>Contacts:</b>
            
            📞 <b>Phone (Telegram, WhatsApp):</b>
            %s
            
            ✉️ <b>Email:</b> %s
            
            You can contact us by phone or email. We are always happy to assist you with any questions!
            """, phone, email);
    }

    @Override
    public String getRules() {
        return """
            <b>House Rules:</b>
            
            1. 🕒 <b>Check-in/Check-out:</b> Check-in is from 14:00, and check-out is by 11:00.
            
            2. 🚭 <b>Smoking:</b> Smoking is prohibited in apartments and common areas. \
            Smoking is allowed only in designated areas.
            
            3. 🧹 <b>Cleanliness:</b> Please maintain cleanliness in the apartments and on \
            the premises. Sort waste and dispose of it in the appropriate containers.
            
            4. 📢 <b>Quiet Hours:</b> Please observe quiet hours from 22:00 to 08:00 to avoid \
            disturbing other residents.
            
            5. 🐾 <b>Pets:</b> Staying with pets is allowed only with prior approval.
            
            6. 🛠 <b>Reporting Issues:</b> If you discover any damage or malfunctions, please \
            notify the administration immediately.
            
            By following these simple rules, you will ensure a comfortable and pleasant \
            stay for yourself and other residents!
            """;
    }

    @Override
    public String getApartments() {
        return """
            <b>Apartment Information:</b>
            
            Our building is a five-story structure equipped with an elevator, \
            providing easy access to every floor.
            
            🏢 <b>General Description:</b> Each floor has 4 apartments, as well as \
            two storage rooms where you can find bathroom supplies.
            
            🛏 <b>Amenities:</b>
            - Modern finishes and high-quality materials
            - Air conditioning
            - Electric stove
            - Refrigerator
            - Dishwasher
            - Electric kettle
            - Dishware set* (5 mugs, 3 spoons, 3 forks, 3 teaspoons, 3 bowls, 1 large plate, \
            1 deep bowl, 2 pots (large and small), 1 frying pan)
            - Ironing board
            - Iron
            - Washing machine
            - Shower
            - Toilet
            
            Enjoy the comfort and safety of our building, designed for your convenience and pleasant living!
            
            *The dishware set may vary from one apartment to another, but will generally resemble \
            the described set.
            """;
    }

    @Override
    public String getRentAnApartment() {
        return """
            <b>Rental Process:</b>
            
            To rent an apartment in our building, please follow these steps:
            
            1. 📋 <b>Fill Out the Application:</b> Provide your name, \
            contact details, and preferred move-in date. We will contact you to clarify the details.
            
            2. 💬 <b>Discuss the Terms:</b> Our agent will get in touch with you to discuss the rental terms.
            
            3. 📝 <b>Sign the Agreement:</b> We will prepare the rental agreement and send it to you for \
            signing. You can also ask any questions you may have.
            
            4. 🔑 <b>Move-In:</b> After signing the agreement and making the payment, \
            you will receive the keys to your new apartment and can move in.
            
            If you have any questions at any stage of the rental process, please contact us. \
            We are always happy to help!
            """;
    }

    @Override
    public String getChangeLanguage() {
        return """
            <b>Localization Selection</b>
            
            🏁 Please choose the language that will be convenient for you to use!
            """;
    }

    @Override
    public String getButtonBack() {
        return "⬅️ Back";
    }

    @Override
    public String getButtonApartments() {
        return "\uD83C\uDFE0 Apartments";
    }

    @Override
    public String getButtonRentAnApartment() {
        return "\uD83D\uDD11 Rent an apartment";
    }

    @Override
    public String getButtonHouseInformation() {
        return "ℹ️ House information";
    }

    @Override
    public String getButtonContacts() {
        return "\uD83D\uDCDE Contacts";
    }

    @Override
    public String getButtonChangeLanguage() {
        return "\uD83C\uDDEC\uD83C\uDDE7 English";
    }

    @Override
    public String getButtonRules() {
        return "\uD83D\uDEAD Rules";
    }

    @Override
    public String getButtonFillOutAnApplication() {
        return "\uD83D\uDD8A Fill out an application";
    }
}
