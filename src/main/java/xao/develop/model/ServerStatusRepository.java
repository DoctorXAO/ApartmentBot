package xao.develop.model;

import org.springframework.data.repository.CrudRepository;

public interface ServerStatusRepository extends CrudRepository<ServerStatus, String> {
    ServerStatus getByCode(String code);
}
