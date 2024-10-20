package xao.develop.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xao.develop.model.TempNewAmenity;
import xao.develop.model.TempNewAmenityRepository;

import java.util.NoSuchElementException;

@Slf4j
@Repository
public class TempNewAmenityPersistence {

    @Autowired
    private final TempNewAmenityRepository tempNewAmenityRepository;

    public TempNewAmenityPersistence(TempNewAmenityRepository tempNewAmenityRepository) {
        this.tempNewAmenityRepository = tempNewAmenityRepository;
    }

    /** Insert new entity to TempNewAmenity **/
    public void insert(long chatId) {
        TempNewAmenity amenity = new TempNewAmenity();

        amenity.setChatId(chatId);

        tempNewAmenityRepository.save(amenity);

        log.debug("New amenity from user {} inserted successfully", chatId);
    }

    /** Select entity from TempNewAmenity **/
    public TempNewAmenity select(long chatId) throws NoSuchElementException {
        return tempNewAmenityRepository.findById(chatId).orElseThrow();
    }

    /** Update link of TempNewAmenity **/
    public void updateLink(long chatId, String link) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setLink(link);

            tempNewAmenityRepository.save(amenity);

            log.debug("Link of TempNewAmenity with ChatId {} updated successfully! New link {}", chatId, link);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateLink(long, String).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Update english name entity of TempNewAmenity **/
    public void updateEn(long chatId, String en) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setEn(en);

            tempNewAmenityRepository.save(amenity);

            log.debug("English name of TempNewAmenity with ChatId {} updated successfully! New english name {}", chatId, en);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateEn(long, String).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Update turkish name entity of TempNewAmenity **/
    public void updateTr(long chatId, String tr) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setTr(tr);

            tempNewAmenityRepository.save(amenity);

            log.debug("Turkish name of TempNewAmenity with ChatId {} updated successfully! New turkish name {}", chatId, tr);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateTr(long, String).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Update ukrainian name entity of TempNewAmenity **/
    public void updateUk(long chatId, String uk) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setUk(uk);

            tempNewAmenityRepository.save(amenity);

            log.debug("Ukrainian name of TempNewAmenity with ChatId {} updated successfully! New ukrainian name {}", chatId, uk);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateUk(long, String).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Update russian name entity of TempNewAmenity **/
    public void updateRu(long chatId, String ru) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setRu(ru);

            tempNewAmenityRepository.save(amenity);

            log.debug("Russian name of TempNewAmenity with ChatId {} updated successfully! New russian name {}", chatId, ru);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateRu(long, String).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Update importance of entity of TempNewAmenity **/
    public void updateImportance(long chatId, int importance) {
        try {
            TempNewAmenity amenity = tempNewAmenityRepository.findById(chatId).orElseThrow();

            amenity.setImportance(importance);

            tempNewAmenityRepository.save(amenity);

            log.debug("Importance of TempNewAmenity with ChatId {} updated successfully! New importance {}", chatId, importance);
        } catch (NoSuchElementException ex) {
            log.error("""
                    Can't find entity of TempNewAmenity with ChatId {} from method updateImportance(long, int).
                    Exception: {}""", chatId, ex.getMessage());
        }
    }

    /** Delete entity of TempNewAmenity **/
    public void delete(long chatId) {
        tempNewAmenityRepository.deleteById(chatId);

        log.debug("Entity of TempNewAmenity with ChatId {} deleted successfully!", chatId);
    }
}
