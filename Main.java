import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xpath.internal.SourceTree;


class GetThread extends Thread {
    private int n;
    private String login;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                URL url = new URL("http://localhost:8080/get?from=" + n);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                InputStream is = http.getInputStream();
                try {
                    int sz = is.available();
                    if (sz > 0) {
                        Gson gson = new GsonBuilder().create();
                        Message[] list = gson.fromJson(new InputStreamReader(is), Message[].class);

                        for (Message m : list) {
                            if (m.getTo().isEmpty() || m.getTo().equals(login)) {
                                System.out.println(m);
                                n++;
                            } else {
                                n++;
                            }
                        }
                    }
                } finally {
                    is.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            boolean r = false;
            String login = new String();
            while (r == false) {
                System.out.println("Enter login: ");
                login = scanner.nextLine();
                System.out.println("Enter password: ");
                String pass = scanner.nextLine();
                Message mess = new Message();
                mess.setFrom(login);
                mess.setText(pass);
                try {
                    int resp = mess.send("http://localhost:8080/login");
                    if (resp == 200) {
                        System.out.println("OK");
                        r = true;
                    } else {
                        System.out.println("Access denied");
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            GetThread th = new GetThread();
            th.setDaemon(true);
            th.start();
            th.setLogin(login);

            help();

            while (true) {
                String text = scanner.nextLine();
                System.out.println("Enter 'username' for private message or verify users'status (or press Enter):");
                String to = scanner.nextLine();
                if (text.isEmpty())
                    break;

                Message m = new Message();
                m.setText(text);
                m.setFrom(login);
                m.setTo(to);
                try {
                    switch (text) {
                        case "users": {
                            m.send("http://localhost:8080/users");
                            break;
                        }
                        case "status": {
                            m.send("http://localhost:8080/status");
                            break;
                        }
                        default: {
                            int res;
                            if (to.contains("#")) {
                                res = m.send("http://localhost:8080/chat");
                            } else {
                                res = m.send("http://localhost:8080/add");
                            }
                            if (res != 200) {
                                System.out.println("HTTP error: " + res);
                                return;
                            }
                            break;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Error: " + ex.getMessage());
                    return;
                }
            }
        } finally {
            scanner.close();
        }
    }

    public static void help() {
        System.out.println("Enter 'users' - to show a list of users.");
        System.out.println("Enter 'status' - to verify the user's status.");
    }
}
