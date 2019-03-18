package project.DAO;


import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//import project.models.Post;
//import project.models.SlugOrID;
//import project.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;

import project.models.Thread;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;


@Service
public class ThreadDAO {

    private final JdbcTemplate template;

    @Autowired
    public ThreadDAO(JdbcTemplate template) {
        this.template = template;

    }

    public Integer[] createThread(Thread body) {
        Integer[] result = {0, 0};
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement pst = con.prepareStatement(
                        "insert into thread(slug, forum, title, message, owner, votes, created)"
                                + " values(?,?,?,?,?,?,?::timestamptz)" + " returning tid",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, body.getSlug());
                pst.setString(2, body.getForum());
                pst.setString(3, body.getTitle());
                pst.setString(4, body.getMessage());
                pst.setString(5, body.getAuthor());
                pst.setLong(6, body.getVotes());
                pst.setString(7, body.getCreated());
                return pst;
            }, keyHolder);
            result[0] = 201;
            result[1] = keyHolder.getKey().intValue();
            return result;
        } catch (DuplicateKeyException e) {
            result[0] = 409;
            return result;
        } catch (DataAccessException e) {
            result[0] = 404;
            return result;
        }
    }

    public List<Thread> getThreads(Integer forumid, Integer limit, String since, Boolean desc) {
        try {
            List<Object> myObj = new ArrayList<>();
            final StringBuilder myStr = new StringBuilder("select * from thread where forumid = ? ");
            myObj.add(forumid);
            if (since != null) {
                if (desc) {
                    myStr.append(" and created <= ?::timestamptz ");
                } else {
                    myStr.append(" and created >= ?::timestamptz ");
                }
                myObj.add(since);
            }
            myStr.append(" order by created ");
            if (desc) {
                myStr.append(" desc ");
            }
            if (limit != null) {
                myStr.append(" limit ? ");
                myObj.add(limit);
            }
            return template.query(myStr.toString()
                    , myObj.toArray(), THREAD_MAPPER);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private static final RowMapper<Thread> THREAD_MAPPER = (res, num) -> {
        long votes = res.getLong("votes");
        Long id = res.getLong("tid");
        Long forumid = res.getLong("forumid");
        String slug = res.getString("slug");
        String owner = res.getString("owner");
        String forum = res.getString("forum");
        Timestamp created = res.getTimestamp("created");
        String message = res.getString("message");
        String title = res.getString("title");
        return new Thread(slug, forum, title, message, owner, id, votes, created, forumid);
    };
}
