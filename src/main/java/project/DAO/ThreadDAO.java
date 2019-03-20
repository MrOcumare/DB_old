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
import project.models.User;
import project.models.Vote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper;

import javax.sound.midi.SysexMessage;


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
                String sql = "UPDATE forum "+
                        "set threadcount = threadcount + 1 "+
                        "WHERE slug = ?::citext";
                template.update(sql, body.getForum());
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

    public List<Thread> getThreads(String forum, Integer limit, String since, Boolean desc) {
        try {
            //System.out.println(forumid + " " + limit + " " + since + " " + desc );
            List<Object> myObj = new ArrayList<>();
            final StringBuilder myStr = new StringBuilder("select * from thread where forum = ?::citext ");
            myObj.add(forum);
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
            //System.out.println(myStr.toString());
            return template.query(myStr.toString()
                    , myObj.toArray(), THREAD_MAPPER);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Thread getThreadbySlugOrID(String key) {
        try {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE tid = ?",
                    THREAD_MAPPER, Integer.parseInt(key));
        } catch (NumberFormatException e) {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE slug = ?::citext",
                    THREAD_MAPPER, key);
        }
    }
    public Thread getThreadById(long id) {
        try {
            return template.queryForObject(
                    "SELECT * FROM thread WHERE tid = ?",
                    THREAD_MAPPER, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void vote(String key, Vote vt) {
//        try{
//            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
//            template.update(con -> {
//                PreparedStatement statement = con.prepareStatement(
//                        "insert into vote(owner, voice) values(?::citext,?) " +
//                                " ON CONFLICT (owner) DO UPDATE SET " +
//                                " voice = ?;",
//                        PreparedStatement.RETURN_GENERATED_KEYS);
//                statement.setString(1, vt.getNickname());
//                statement.setLong(2, vt.getVoice());
//                statement.setLong(3, vt.getVoice());
//                return statement;
//            }, keyHolder);
//            String sql = "UPDATE thread "+
//                    "set votes = votes + ? "+
//                    "WHERE tid = ?";
//            template.update(sql, vt.getVoice(), Integer.parseInt(key) );
//            System.out.println(Integer.parseInt(key)+"    qq    weqwqeqweqweqweqweqe     "  + vt.getVoice());
//        } catch (NumberFormatException e) {
//            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
//            template.update(con -> {
//                PreparedStatement statement = con.prepareStatement(
//                        "insert into vote(owner, voice) values(?::citext,?)" +
//                                " ON CONFLICT (owner) DO UPDATE SET " +
//                                " voice = ?;",
//                        PreparedStatement.RETURN_GENERATED_KEYS);
//                statement.setString(1, vt.getNickname());
//                statement.setLong(2, vt.getVoice());
//                statement.setLong(3, vt.getVoice());
//
//                return statement;
//            }, keyHolder);
//            String sql = "UPDATE thread "+
//                    "set votes = votes + ? "+
//                    "WHERE slug = ?::citext";
//            template.update(sql, vt.getVoice(), key );
//        }
        try {
            vt.setTid(Integer.parseInt(key));
        } catch (NumberFormatException e) {
            vt.setTid(
                    template.queryForObject("SELECT tid FROM thread WHERE slug = ?::citext", new Object[]{key}, Integer.class));
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> {
            PreparedStatement statement = con.prepareStatement(
                    "insert into vote(owner, voice, tid) values(?::citext,?, ?)" +
                            " ON CONFLICT (owner) DO UPDATE SET " +
                            " voice = ?;",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, vt.getNickname());
            statement.setLong(2, vt.getVoice());
            statement.setLong(3, vt.getTid());
            statement.setLong(4, vt.getVoice());

            return statement;
        }, keyHolder);
        String sql = "UPDATE thread set votes = (select sum(voice) from vote WHERE tid = ?) WHERE tid = ?";
        template.update(sql, vt.getTid(), vt.getTid());
    }

    public Integer chagenThread(Thread body) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement pst = con.prepareStatement(
                        "update thread set" +
                                "  message = COALESCE(?, message)," +
                                "  title = COALESCE(?, title)" +
                                "where tid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, body.getMessage());
                pst.setString(2, body.getTitle());
                pst.setLong(3, body.getId());
                return pst;
            }, keyHolder);
            System.out.println(body.getForum() + "         ");
        } catch (Exception e) {
            return 409;
        }
        return 201;
    }


    //    public static final RowMapper<Vote> VOTE_MAPPER = (res, num) -> {
//        String nickname = res.getString("nickname");
//        Long vote = res.getLong("vote");
//        return new Vote(nickname, vote);
//    };
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
