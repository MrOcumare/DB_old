package project.DAO;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import project.models.Forum;
import project.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;

@Service
public class ForumDAO {
    private final JdbcTemplate template;

    public ForumDAO(JdbcTemplate template) {

        this.template = template;
    }

    public Integer createForum(Forum body) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO forum(slug, title, owner)" + " VALUES(?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getSlug());
                statement.setString(2, body.getTitle());
                statement.setString(3, body.getUser());
                return statement;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            return 409;
        } catch (DataAccessException e) {
            return 404;
        }
        return 201;
    }
    public Forum getConflictForum(String username) {
//        try {
//        final StringBuilder query =
//                new StringBuilder("SELECT * from forum as f where f.owner = ?::citext;");
//        return template.query(query.toString(), FORUM_MAPPER);

        return template.queryForObject(
                "SELECT * from forum as f where f.owner = ?::citext;",
                FORUM_MAPPER, username);
//        } catch (DataAccessException e) {
//            return null;
//        }
    }

    public Forum getForum(String slug) {
        try {
            return template.queryForObject(
                    "SELECT * from forum as f where f.slug = ?::citext;",
                    FORUM_MAPPER, slug);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Integer getForumBySlug(String slug) {
//        List<Object> obj = new ArrayList<>();
//        obj.add(slug);
//        final StringBuilder query =
//                new StringBuilder("SELECT * from forum as f where f.slug = ?::citext;");
//        return (template.query(query.toString(), obj.toArray(), FORUM_MAPPER).size());
        return template.queryForObject(
                "SELECT id FROM forum WHERE slug = ?::citext",
                Integer.class, slug);
    }

    public static final RowMapper<Forum> FORUM_MAPPER = (res, num) -> {
        String slug = res.getString("slug");
        String title = res.getString("title");
        String user = res.getString("owner");

        return new Forum(slug, title, user);
    };
}
