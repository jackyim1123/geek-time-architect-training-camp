import java.util.ArrayList;

interface Component {
    public void print();
}

class Control implements Component {
    private String name;

    public Control(String name) {
        this.name = name;
    }

    @Override
    public void print() {
        System.out.println("print" + " " + this.name);
    }
}

class Container implements Component {
    private String name;
    private final ArrayList<Component> list = new ArrayList<>();

    public Container(String name) {
        this.name = name;
    }

    public void add(Component c) {
        list.add(c);
    }

    @Override
    public void print() {
        System.out.println("print" + " " + this.name);
        for (Component component : list) {
            component.print();
        }
    }
}

class PrintWinForm {
    public static void main(String[] args) {
        Container winForm = new Container("WinForm(WINDOW窗口)"); 
        Control picture = new Control("Picture(LOGO图片)");
        Control btnLogin = new Control("Button(登录)");
        Control btnReg = new Control("Button(注册))");        
        Container frame = new Container("Frame(FEAME1)");
        winForm.add(picture);
        winForm.add(btnLogin);
        winForm.add(btnReg);
        winForm.add(frame);

        Control lblUsername = new Control("Label(用户名)");
        Control txtUsername = new Control("TextBox(文本框)");
        Control lblPassword = new Control("Label(密码)");
        Control txtPassword = new Control("PasswordBox(密码框)");
        Control chkRememberPassword = new Control("CheckBox(复选框)");
        Control lblRememberPassword = new Control("TextBox(记住用户名)");
        Control lblForgotPassword = new Control("LinkLabel(忘记密码)");
        frame.add(lblUsername);
        frame.add(txtUsername);
        frame.add(lblPassword);
        frame.add(txtPassword);
        frame.add(chkRememberPassword);
        frame.add(lblRememberPassword);
        frame.add(lblForgotPassword);

        winForm.print();
    }
}