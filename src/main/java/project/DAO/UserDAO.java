package project.DAO;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import project.models.User;
import project.utils.Response;

import java.sql.PreparedStatement;

@Service
//@Transactional
public class UserDAO {

    private final JdbcTemplate template;

    public UserDAO(JdbcTemplate template) {
        this.template = template;
    }

    public Response<User> createUser(User body)  {
        Response<User> result = new Response<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
        template.update(con -> {
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO users(fullname, nickname, email, about)" + " VALUES(?,?,?,?)" ,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, body.getFullname());
            statement.setString(2, body.getNickname());
            statement.setString(3, body.getEmail());
            statement.setString(4, body.getAbout());
            return statement;
        }, keyHolder);
        result.setResponse(body, HttpStatus.CREATED);
        return result;
        }
        catch (DuplicateKeyException e) {
            result.setResponse(body, HttpStatus.CONFLICT);
            return result;
        }
    }

    public static final RowMapper<User> userMapper = (res, num) -> {
        String nickname = res.getString("nickname");
        String email = res.getString("email");
        String fullname = res.getString("fullname");
        String about = res.getString("about");
        if (res.wasNull()) {
            about = null;
        }
        return new User(fullname, nickname, email, about);
    };

}
