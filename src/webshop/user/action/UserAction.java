package webshop.user.action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.ServletActionContext;
import webshop.user.service.UserService;
import webshop.user.vo.User;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserAction extends ActionSupport implements ModelDriven<User> {

    // 模型驱动使用的对象
    private User user = new User();
    private UserService userService;
    // 接收验证码
    private String checkcode;

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User getModel() {
        return user;
    }

    /**
     * 跳转到注册页面的执行方法
     * @return
     */
    public String registPage() {
        return "registPage";
    }


    /**
     * AJAX进行异步校验的执行方法
     * @return
     */
    public String findByName() throws IOException {
        // 调用service进行查询
        User existUser = userService.findByUsername(user.getUsername());
        // 获得response对象,向页面输出
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        // 判断
        if (existUser != null) {
            // 查询到用户: 用户名已经存在
            response.getWriter().println("<font color='red'>用户名已经存在</font>");
        } else {
            // 没有查询到该用户: 用户名可以使用
            response.getWriter().println("<font color='green'>用户名可以使用</font>");
        }
        return NONE;
    }

    /**
     * 用户注册方法
     * @return
     */

    public String regist() {
        // 判断验证码程序:
        // 从session中获得验证码的随机值:
        String checkcode1 = (String) ServletActionContext.getRequest().getSession().getAttribute("checkcode");
        if (!checkcode.equalsIgnoreCase(checkcode1)) {
            this.addActionError("验证码输入错误!");
            return "checkCodeFail";
        }
        userService.save(user);
        this.addActionMessage("注册成功,请去邮箱激活");
        return "msg";
    }

    /**
     * 用户激活的方法
     */
    public String active() {

        // 根据激活码查询用户
        User existUser = userService.findByCode(user.getCode());

        if (existUser == null) {
            // 激活码错误的情况
            this.addActionMessage("激活失败:激活码错误!");
        } else {
            // 激活成功
            // 修改用户的状态
            existUser.setState(1);
            existUser.setCode(null);
            userService.update(existUser);
            this.addActionMessage("激活成功: 您可以登入了");
        }
        return "msg";
    }

    /**
     * 跳转到登入页面
     */
    public String loginPage() {
        return "loginPage";
    }

    /**
     * 登入的方法
     */
    public String login() {
        User existUser = userService.login(user);
        if (existUser == null) {
            this.addActionError("登录失败:用户未注册或密码错误");
            return LOGIN;
        } else {
            // 登录成功
            // 将用户的信息存入到session中
            ServletActionContext.getRequest().getSession().setAttribute("existUser", existUser);
            // 页面跳转
            return "loginSuccess";
        }
    }

    /**
     * 用户退出的方法
     */
    public String quit() {
        // 销毁session
        ServletActionContext.getRequest().getSession().invalidate();
        return "quit";
    }
}
