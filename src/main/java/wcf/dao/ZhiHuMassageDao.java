package wcf.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import wcf.entity.ZhiHuMassage;

import java.util.List;


/**
 * @author 43574
 */
public interface ZhiHuMassageDao extends CrudRepository<ZhiHuMassage, Long> {

    /**
     * 获取所有id
     * @return 所有id
     */
    @Query(value = "SELECT id FROM ZhiHuMassage")
    List<Long> getIds();
}
