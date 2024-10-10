package xao.develop.model;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TempAdminSettingsRepository extends CrudRepository<TempAdminSettings, Long> {
    TempAdminSettings findById(long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE TempAdminsSettings a SET " +
            "a.selectedApplication = 0, " +
            "a.selectedPage = 0, " +
            "a.isNewApartment = false, " +
            "a.isCheckingSelectedAmenities = false")
    void resetToDefault();
}
