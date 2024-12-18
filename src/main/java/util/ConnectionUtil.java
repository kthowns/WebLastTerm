package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import code.ApplyStatus;
import entity.Apl;
import entity.Band;
import entity.Comment;
import entity.Hashtag;
import entity.MemberDetail;
import entity.Post;
import entity.Recruit;
import entity.User;

public class ConnectionUtil {
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private String driver = "com.mysql.cj.jdbc.Driver";
	String url = "jdbc:mysql://127.0.0.1:3306/myband?characterEncoding=UTF-8&useUnicode=true";
	private String id = "myband";
	private String pass = "myband";
	private Boolean isTransactional = false;

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
	
	public void openTransactional() throws SQLException {
		this.isTransactional = true;
		this.conn = DriverManager.getConnection(this.url, this.id, this.pass);
		if(this.conn != null && !this.conn.isClosed()) {
			this.conn.setAutoCommit(false);
		}
	}
	
	public void commitTransactional() throws SQLException {
		this.isTransactional = false;
		this.conn.commit();
		this.conn.setAutoCommit(true);
		this.conn.close();
	}
	
	private void closeTransactional() throws SQLException {
		this.conn.rollback();
		this.isTransactional = false;
		this.conn.setAutoCommit(true);
		this.conn.close();
	}
	
	public Integer requestInsert() throws SQLException {
		Integer result = 0;
		try {
			this.pstmt.executeUpdate();
			ResultSet rs = this.pstmt.getGeneratedKeys();
			while(rs.next()) {
				result = rs.getInt(1);
			}
		} catch(Exception e) {
			if(isTransactional) {
				if(conn != null) {
					closeTransactional();
					throw e;
				}
			} else {
				throw e;
			}
		} finally {
			if(!isTransactional) {
				this.conn.close();
			}
		}
		return result;
	}
	
	public <T> Integer requestUpdate(Class<T> cls) throws SQLException {
		Integer result = 0;
		try {
			result = this.pstmt.executeUpdate();
		} catch(Exception e) {
			if(isTransactional) {
				if(conn != null) {
					closeTransactional();
					throw e;
				}
			} else {
				throw e;
			}
		} finally {
			if(!isTransactional) {
				this.conn.close();
			}
		}
		return result;
	}
	
	public Integer requestInt() throws SQLException {
		ResultSet rs = this.pstmt.executeQuery();
		try {
			while (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			if(!isTransactional) {
				this.conn.close();
			}
		}
		return -1;
	}

	public <T> Optional<T> request(Class<T> cls) throws SQLException {
		ResultSet rs = this.pstmt.executeQuery();
		try {
			while (rs.next()) {
				return mapToOptional(rs, cls);
			}
		} finally {
			if(!isTransactional) {
				this.conn.close();
			}
		}
		return Optional.empty();
	}

	public <T> List<T> requestForList(Class<T> cls) throws SQLException {
		ResultSet rs = this.pstmt.executeQuery();
		List<T> result = new ArrayList<>();
		try {
			while (rs.next()) {
				result.add(mapToOptional(rs, cls)
						.orElseThrow(() -> new SQLException("Handling response error")));
				}
		} finally {
			if(!isTransactional) {
				this.conn.close();
			}
		}
		return result;
	}
	
	public PreparedStatement setInsertQuery(String query) throws ClassNotFoundException, SQLException {
		Class.forName(this.driver);
		if(this.conn == null || this.conn.isClosed()) {
			this.conn = DriverManager.getConnection(this.url, this.id, this.pass);
		}
		this.pstmt = this.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		return this.pstmt;
	}

	public PreparedStatement setQuery(String query) throws SQLException, ClassNotFoundException {
		Class.forName(this.driver);
		if(this.conn == null || this.conn.isClosed()) {
			this.conn = DriverManager.getConnection(this.url, this.id, this.pass);
		}
		this.pstmt = this.conn.prepareStatement(query);
		return this.pstmt;
	}
	
	private <T> Optional<T> mapToOptional(ResultSet rs, Class<T> cls) throws SQLException{
		try {
			if (cls.isAssignableFrom(User.class)) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setEmail(rs.getString("email"));
				user.setUsername(rs.getString("username"));
				user.setPassword(rs.getString("password"));
				System.out.println(user.toString());
				return Optional.of(cls.cast(user));
			} else if (cls.isAssignableFrom(Band.class)) {
				Band band = new Band();
				band.setId(rs.getInt("id"));
				band.setLeaderId(rs.getInt("leader_id"));
				band.setName(rs.getString("name"));
				band.setDescription(rs.getString("description"));
				return Optional.of(cls.cast(band));
			} else if (cls.isAssignableFrom(Apl.class)) {
				Apl apl = new Apl();
				apl.setAge(rs.getInt("age"));
				apl.setLocation(rs.getString("location"));
				apl.setName(rs.getString("name"));
				apl.setPhone(rs.getString("phone"));
				apl.setApplicantId(rs.getInt("applicant_id"));
				apl.setRecruitId(rs.getInt("recruit_id"));
				apl.setStatus(ApplyStatus.valueOf(rs.getString("status")));
				apl.setPosition(rs.getString("position"));
				apl.setDescription(rs.getString("description"));
				apl.setCreatedAt(rs.getTimestamp("created_at"));
				return Optional.of(cls.cast(apl));
			} else if (cls.isAssignableFrom(Comment.class)) {
				Comment comment = new Comment();
				comment.setId(rs.getInt("id"));
				comment.setPostId(rs.getInt("post_id"));
				comment.setAuthorId(rs.getInt("author_id"));
				comment.setContent(rs.getString("content"));
				comment.setCreatedAt(rs.getTimestamp("created_at"));
				return Optional.of(cls.cast(comment));
			} else if (cls.isAssignableFrom(Hashtag.class)) {
				Hashtag hashtag = new Hashtag();
				hashtag.setId(rs.getInt("id"));
				hashtag.setPostId(rs.getInt("post_id"));
				hashtag.setHashtag(rs.getString("hashtag"));
				return Optional.of(cls.cast(hashtag));
			} else if (cls.isAssignableFrom(Post.class)) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setAuthorId(rs.getInt("author_id"));
				post.setBandId(rs.getInt("band_id"));
				post.setTitle(rs.getString("title"));
				post.setContent(rs.getString("content"));
				post.setViews(rs.getInt("views"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				return Optional.of(cls.cast(post));
			} else if (cls.isAssignableFrom(Recruit.class)) {
				Recruit recruit = new Recruit();
				recruit.setId(rs.getInt("id"));
				recruit.setPostId(rs.getInt("post_id"));
				recruit.setBandId(rs.getInt("band_id"));
				recruit.setPosition(rs.getString("position"));
				recruit.setAcceptedId(rs.getInt("accepted_id"));
				recruit.setCreatedAt(rs.getTimestamp("created_at"));
				return Optional.of(cls.cast(recruit));
			}else if (cls.isAssignableFrom(MemberDetail.class)) {
				MemberDetail md = new MemberDetail();
				md.setId(rs.getInt("id"));
				md.setBandId(rs.getInt("band_id"));
				md.setName(rs.getString("name"));
				md.setPosition(rs.getString("position"));
				md.setAge(rs.getInt("age"));
				md.setPhone(rs.getString("phone"));
				return Optional.of(cls.cast(md));
			}
		} catch (Exception e) {
			throw new SQLException("Response mapping error", e);
		}
		return Optional.empty();
	}
}
