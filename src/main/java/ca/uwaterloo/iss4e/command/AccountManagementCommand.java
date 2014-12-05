package ca.uwaterloo.iss4e.command;

import ca.uwaterloo.iss4e.common.SMASException;
import ca.uwaterloo.iss4e.common.Utils;
import ca.uwaterloo.iss4e.databases.AccountDAO;
import ca.uwaterloo.iss4e.databases.Impl.AccountDAOImpl;
import ca.uwaterloo.iss4e.databases.Impl.MessageDAOImpl;
import ca.uwaterloo.iss4e.databases.Impl.UserDAOImpl;
import ca.uwaterloo.iss4e.databases.MessageDAO;
import ca.uwaterloo.iss4e.databases.UserDAO;
import ca.uwaterloo.iss4e.dto.Account;
import ca.uwaterloo.iss4e.dto.UserInfo;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyright (c) 2014 Xiufeng Liu ( xiufeng.liu@uwaterloo.ca )
 * <p/>
 * This file is free software: you may copy, redistribute and/or modify it
 * under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 * <p/>
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */


public class AccountManagementCommand implements Command {
    private static final Logger log = Logger.getLogger(AccountManagementCommand.class.getName());
    private String registerPage;
    private String successPage;

    AccountDAO dao = new AccountDAOImpl();


    public AccountManagementCommand(String[] pages) {
        this.registerPage = pages[0];
        this.successPage = pages[1];
    }

    public void getForm(ServletContext ctx, HttpServletRequest request, HttpServletResponse response, JSONObject out) throws ServletException, IOException {
        request.setAttribute("account", new Account());
        request.getRequestDispatcher(this.registerPage).forward(request, response);
    }

    public void create(ServletContext ctx, HttpServletRequest request, HttpServletResponse response, JSONObject out) throws ServletException, IOException {
        Account account = new Account();
        StringBuffer buf = new StringBuffer();
        String username = request.getParameter("username");
        boolean success = true;
        buf.append("<div class='alert alert-danger' data-dismiss='alert'>");
        if (Utils.isEmpty(username)) {
            buf.append("<p>Username cannot be empty</p>");
            success = false;
        }
        String password = request.getParameter("password");
        String retypePassword = request.getParameter("retypepassword");
        if (Utils.isEmpty(password) && Utils.isEmpty(retypePassword)) {
            buf.append("<p>Password cannot be empty</p>");
            success = false;
        } else if (!password.equals(retypePassword)) {
            buf.append("<p>The password does not match</p>");
            success = false;
        }
        String firstName = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");
        String email = request.getParameter("email");

        account.setUsername(username);
        account.setPassword(password);
        account.setRetypePassword(retypePassword);
        account.setFirstName(firstName);
        account.setLastName(lastname);
        account.setEmail(email);

        try {
            if (success) {
                dao.create(account, out);
            }
        } catch (Exception e) {
            buf.append(e.getMessage());
            success = false;
        }
        buf.append("</div>");
        account.setMessage(buf.toString());
        request.setAttribute("account", account);
        request.getRequestDispatcher(success ? this.successPage : this.registerPage).forward(request, response);
    }

    public void read(ServletContext ctx, HttpServletRequest request, HttpServletResponse response, JSONObject out) throws ServletException, IOException, SMASException {
        int offset = Integer.parseInt(request.getParameter("offset"));
        dao.read(0, out);
    }

    public void delete(ServletContext ctx, HttpServletRequest request, HttpServletResponse response, JSONObject out) throws ServletException, IOException, SMASException {
        String userIDStr = request.getParameter("userIDs");
        String[] userIDArray = Utils.splitToArray(userIDStr, ",", true);
        if (userIDArray.length > 0) {
            dao.delete(userIDArray, out);
        } else {
            throw new SMASException("Please select the accounts to be deleted!");
        }
    }
}
