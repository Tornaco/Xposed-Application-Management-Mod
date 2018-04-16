package github.tornaco.apigen.service.github.bean;

/**
 * Created by Tornaco on 2018/4/16 14:45.
 * God bless no bug!
 */

//{
//        "login": "Tornaco",
//        "id": 23255526,
//        "avatar_url": "https://avatars0.githubusercontent.com/u/23255526?v=4",
//        "gravatar_id": "",
//        "url": "https://api.github.com/users/Tornaco",
//        "html_url": "https://github.com/Tornaco",
//        "followers_url": "https://api.github.com/users/Tornaco/followers",
//        "following_url": "https://api.github.com/users/Tornaco/following{/other_user}",
//        "gists_url": "https://api.github.com/users/Tornaco/gists{/gist_id}",
//        "starred_url": "https://api.github.com/users/Tornaco/starred{/owner}{/repo}",
//        "subscriptions_url": "https://api.github.com/users/Tornaco/subscriptions",
//        "organizations_url": "https://api.github.com/users/Tornaco/orgs",
//        "repos_url": "https://api.github.com/users/Tornaco/repos",
//        "events_url": "https://api.github.com/users/Tornaco/events{/privacy}",
//        "received_events_url": "https://api.github.com/users/Tornaco/received_events",
//        "type": "User",
//        "site_admin": false,
//        "contributions": 753
//        },
public class Contributor {

    private String login, avatar_url, html_url, id;
    private int contributions;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getContributions() {
        return contributions;
    }

    public void setContributions(int contributions) {
        this.contributions = contributions;
    }
}
