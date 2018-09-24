package vitamio.vitamiolibrary;


class Login1
{
    String username;
    String password;
    int is_anchor;
    public Login1(String username, String password) {
        this.username = username;
        this.password = password;
        this.is_anchor = 0;
    }
}

class Register1
{
    String username;
    String password;
    String email;
    int is_anchor;
    public Register1(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.is_anchor = 0;
    }

}

class Forget
{
    String username;
    String email;
    int is_anchor;
    public Forget(String username, String email) {
        this.username = username;
        this.email = email;
        this.is_anchor = 0;
    }
}



class Forget2
{
    String username;
    String password;
    int is_anchor;
    public Forget2(String username, String password) {

        this.username = username;
        this.password = password;
        this.is_anchor = 0;
    }

}

class ResetPwd1
{
    String username;
    String oldPwd;
    String newPwd;
    int is_anchor;
    public ResetPwd1(String username, String oldPwd, String newPwd) {
        this.username = username;
        this.oldPwd = oldPwd;
        this.newPwd = newPwd;
        this.is_anchor = 0;
    }
}

class Check1
{
    int is_anchor = 0;
    public Check1(int is_anchor) {
        this.is_anchor = is_anchor;
    }

}