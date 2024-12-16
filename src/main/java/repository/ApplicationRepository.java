package repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import entity.Application;
import util.ConnectionUtil;

public class ApplicationRepository {
    private final ConnectionUtil connUtil = new ConnectionUtil();

    // ID로 Application 찾기
    public Optional<Application> findById(Integer applicantId, Integer recruitId) throws ClassNotFoundException, SQLException {
        PreparedStatement stmt = connUtil.setQuery("SELECT * FROM applications WHERE applicant_id = ? AND recruit_id = ?");
        stmt.setInt(1, applicantId);
        stmt.setInt(2, recruitId);
        return connUtil.request(Application.class);
    }

    // 모든 Application 찾기
    public List<Application> findAll() throws ClassNotFoundException, SQLException {
        connUtil.setQuery("SELECT * FROM applications");
        return connUtil.requestForList(Application.class);
    }

    // Application 추가
    public Optional<Application> add(Application application) throws ClassNotFoundException, SQLException {
        PreparedStatement stmt = connUtil.setQuery("INSERT INTO applications(applicant_id, recruit_id, position, description, name, age, location, phone) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        stmt.setInt(1, application.getApplicantId());
        stmt.setInt(2, application.getRecruitId());
        stmt.setString(3, application.getPosition());
        stmt.setString(4, application.getDescription());
        stmt.setString(5, application.getName());
        stmt.setInt(6, application.getAge());
        stmt.setString(7, application.getLocation());
        stmt.setString(8, application.getPhone());
        if (connUtil.requestUpdate(Application.class) > 0) {
            // 추가된 Application 정보 반환
            PreparedStatement stmt_ = connUtil.setQuery("SELECT * FROM applications WHERE applicant_id = ? AND recruit_id = ?");
            stmt_.setInt(1, application.getApplicantId());
            stmt_.setInt(2, application.getRecruitId());
            return connUtil.request(Application.class);
        }
        return Optional.empty();
    }

    // Application 수정
    public Optional<Application> update(Application application) throws ClassNotFoundException, SQLException {
        PreparedStatement stmt = connUtil.setQuery("UPDATE applications SET status = ?, position = ?, description = ? WHERE applicant_id = ? AND recruit_id = ?");
        stmt.setString(1, application.getStatus().name());
        stmt.setString(2, application.getPosition());
        stmt.setString(3, application.getDescription());
        stmt.setInt(4, application.getApplicantId());
        stmt.setInt(5, application.getRecruitId());
        if (connUtil.requestUpdate(Application.class) > 0) {
            // 수정된 Application 정보 반환
            PreparedStatement stmt_ = connUtil.setQuery("SELECT * FROM applications WHERE applicant_id = ? AND recruit_id = ?");
            stmt_.setInt(1, application.getApplicantId());
            stmt_.setInt(2, application.getRecruitId());
            return connUtil.request(Application.class);
        }
        return Optional.empty();
    }

    // Application 삭제
    public Optional<Application> delete(Application application) throws ClassNotFoundException, SQLException {
        PreparedStatement stmt = connUtil.setQuery("DELETE FROM applications WHERE applicant_id = ? AND recruit_id = ?");
        stmt.setInt(1, application.getApplicantId());
        stmt.setInt(2, application.getRecruitId());
        if (connUtil.requestUpdate(Application.class) > 0) {
            // 삭제된 Application 반환
            return Optional.of(application);
        }
        return Optional.empty();
    }
}
