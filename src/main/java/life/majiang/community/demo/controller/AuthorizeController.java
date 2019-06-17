package life.majiang.community.demo.controller;
import life.majiang.community.demo.dto.AccessTokenDTO;
import life.majiang.community.demo.dto.GithubUser;
import life.majiang.community.demo.model.User;
import life.majiang.community.demo.provider.GithubProvider;
import life.majiang.community.demo.provider.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/*
* 认证controller*/
@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Value("${github.client.id}")//去application.properties配置文件里面读
    private  String clientId;
    @Value("${github.client.secret}")
    private  String clientSecret;
    @Value("${github.redirect.uri}")
    private  String redirectUri;
    @Autowired
    private UserMapper userMapper;
    @GetMapping("/callback")
    public String  callback(@RequestParam(name="code")String code,
                            @RequestParam(name="state")String state,
                            HttpServletResponse response){
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setState(state);
        accessTokenDTO.setClient_secret(clientSecret);
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if(githubUser!=null){
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
            //自动写入cookie
            response.addCookie(new Cookie("token",token));
            /*//登录成功，写cookie和session
            request.getSession().setAttribute("user",user);*/
            return "redirect:/";
        }else{
            //登录失败,重新登录
            return "redirect:/";
        }

    }
}

