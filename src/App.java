import java.util.*;
import java.sql.*;

public class App {
    public static Scanner input = new Scanner(System.in); //User Input for Account

    public static int length;

    public static List<String> lst = new ArrayList<String>(); //list of customer details
    public static List<String> accnts = new ArrayList<String>(); //list of accounts the customer owns
    public static List<String> accntIDs = new ArrayList<String>(); //list of customers account ids 
    public static List<String> banks = new ArrayList<String>(); //list of customers banks 
    public static List<String> perms = new ArrayList<String>(); //list of permissions allowed by current user
    public static Dictionary<List <String>, String> allAccnts = new Hashtable<List<String>, String>(); //Dictionary of key [aid, bank] and value balance
    
    public static void main(String[] args) {
        Login();
        Transaction();
        input.close();
    }

    public static void Login(){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                System.out.println("Enter username:"); String uname = input.nextLine(); //Account Username 
                if(uname.equals("quit")){
                    System.exit(1);
                }
                System.out.println("Enter password:"); String pword = input.nextLine(); //Account Password
                
                String type = null; 

                Statement st1 = conn.createStatement(); // GETS ROWS FROM CUSTOMER TABLE INTO rs1
                ResultSet rs1 = st1.executeQuery("SELECT * FROM customer WHERE username = '" + uname + "' AND password = '" + pword + "'"); // lst.get(3) and (4) are Username and Password
                
                if(rs1.next()){ // Checks if user is part of the customer table
                    type = "Customer";
                    length = rs1.getMetaData().getColumnCount();
                    CustomerAccount(rs1);
                    rs1.close();
                } else {
                    Statement st2 = conn.createStatement(); // GETS ROWS FROM EMPLOYEE AND BRANCH TABLE INTO rs2
                    ResultSet rs2 = st2.executeQuery("SELECT * FROM employee CROSS JOIN branch WHERE worksat = branch.address AND username = '" + uname + "' AND password = '" + pword + "'"); // lst.get(5) and (6) are Username and Password
                
                    if(rs2.next()){ // Checks if user is part of the employee table
                        type = rs2.getString(1);
                        length = rs2.getMetaData().getColumnCount();
                        EmployeeAccount(type, rs2);
                        rs2.close();
                    } else {
                        System.out.println("Account Doesnt Exist");
                        Login();
                    }
                }
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }

    public static void CustomerAccount(ResultSet rs){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                String username = "";
                String name = "";
                lst.add("Customer");
                for(int i = 1; i < length; i++){ //Makes a ArrayList of all of customers details 
                    lst.add(rs.getString(i));
                }
                name = lst.get(2); //name of the user
                username = lst.get(4); //username of the user
                String cid = lst.get(1); //gets the ID of the customer
                
                System.out.println("----------------------");
                System.out.println(name + " Succuesfully Logged in as a Customer"); //Prints the name of customer
                System.out.println("Username: " + username); //Prints the username of customer
                System.out.println("----------------------");
                
                Statement st1 = conn.createStatement(); // GETS ROWS FROM account TABLE INTO rs3 WHERE THE CUSTOMER OWNS THE ACCOUNT
                ResultSet rs1 = st1.executeQuery("SELECT * FROM account WHERE customerID = '" + cid + "'"); 
                int length3 = rs1.getMetaData().getColumnCount();
                System.out.println("--Accounts--");
                while(rs1.next()){ 
                    String temp1 = "";
                    List<String> accnt = new ArrayList<String>(); // Details of Each Individul Bank Account
                    List<String> pair = new ArrayList<String>(); //Pair of Account ID and Bank 
                    for (int i = 1; i <= length3; i++){
                        accnts.add(rs1.getString(i)); //adds each value from table into accnts
                        accnt.add(rs1.getString(i)); //adds each value from table into accnt
                        if(i == 1){
                            accntIDs.add(rs1.getString(i)); //adds all account IDs of the user to accntIds
                            pair.add(rs1.getString(i)); //adds account IDs to the pair
                        }
                        if(i == 2){
                            temp1 = rs1.getString(i); //gets the balance of account
                        }
                        if(i == 5){
                            banks.add(rs1.getString(i)); //adds all the banks that the customer has an account in
                            pair.add(rs1.getString(i)); //adds the bank to the pair
                            allAccnts.put(pair, temp1); //puts the pairs of aIDs and banks into the dictionary as a key with balance as the value
                        }
                    }
                    //System.out.println("Account ID: " + accnt.get(0) +" | Balance: " + accnt.get(1) + " | Type: " + accnt.get(2) + " | " + accnt.get(4)); //Prints the information of each account 
                }
               

                perms.add("Withdraw");
                perms.add("Deposit");
                perms.add("Transfer");
                perms.add("External Transfer");
                perms.add("Logout");
                rs.close(); rs1.close();
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }

    public static void EmployeeAccount(String type, ResultSet rs){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                String username = "";
                String name = "";
                lst.add(type);
                for(int i = 2; i <= length; i++){ //Makes a Arraylist of employees details
                    lst.add(rs.getString(i));
                }
                name = lst.get(1); //gets the employees name
                username = lst.get(5); //gets the employees username
                String bank = lst.get(9); //gets where the employee works
                System.out.println("----------------------");
                System.out.println(name + " Succuesfully Logged in as a " + type); //Prints the name and type of employee
                System.out.println("Username: " + username); //Prints the username
                System.out.println("----------------------");

                Statement st1 = conn.createStatement(); // GETS ROWS FROM EMPLOYEE TABLE INTO rs2
                ResultSet rs1 = st1.executeQuery("SELECT * FROM account WHERE bank = '" + bank + "'"); // arr[5] and [6] are Username and Password
                int length1 = rs1.getMetaData().getColumnCount();
                while(rs1.next()){
                    String temp1 = "";
                    List<String> accnt = new ArrayList<String>(); // Details of Each Individual Bank Account in their bank
                    List<String> pair = new ArrayList<String>(); //Pair of Account ID and Bank 
                    for (int i = 1; i <= length1; i++){
                        accnts.add(rs1.getString(i));  //adds each value from table into accnts
                        accnt.add(rs1.getString(i)); //adds each value from table into accnt
                        if(i == 1){
                            accntIDs.add(rs1.getString(i)); //adds all account IDs of the user to accntIds
                            pair.add(rs1.getString(i)); //adds account IDs to the pair
                        }
                        if(i == 2){
                            temp1 = rs1.getString(i); //gets the balance of account
                        }
                        if(i == 5){
                            pair.add(rs1.getString(i)); //adds the bank to the pair
                            allAccnts.put(pair, temp1); //puts the pairs of aIDs and banks into the dictionary as a key with balance as the value
                        }
                    }
                }
                if (type.equals("Manager")){
                    perms.add("Withdraw");
                    perms.add("Deposit");
                    perms.add("Transfer");
                    perms.add("External Transfer");
                    perms.add("Logout");
                } else if(type.equals("Teller")){
                    perms.add("Withdraw");
                    perms.add("Deposit");
                    perms.add("Transfer");
                    perms.add("External Transfer");
                    perms.add("Logout");
                }
                rs.close(); rs1.close();
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }
   
    public static void Transaction(){
        System.out.println("----------------------");
        int len = accnts.size()/5;
        for(int i = 1; i <= len; i++){
            String aid = accnts.get((5 * i) - 5);
            String bank = accnts.get((5 * i) - 1);
            List<String> key = new ArrayList<String>(); key.add(aid); key.add(bank);
            String balance = allAccnts.get(key);
            String type = accnts.get((5 * i) - 3);
            System.out.println("Account ID: " + aid +" | Balance: " + balance + " | Type: " + type  + " | " + bank); //Prints the information of each account 
        }
        System.out.println("----------------------");

        System.out.print("What would you like to do?: " + perms.get(0)); //Prints out all permissions of the user
        for(int i = 1; i < (perms.size()); i++){
            System.out.print(", " + perms.get(i));
        }
        
        System.out.println("");
        System.out.println("----------------------");
        String action = input.nextLine(); //Gets what action user wants to do (Withdraw, Transfer...)
        
        if(action.equals("Withdraw")){
            Withdraw();
        } else if(action.equals("Deposit")){
            Deposit();
        } else if(action.equals("Transfer")){
            Transfer(false); //false for regular transfer
        } else if(action.equals("External Transfer")){
            Transfer(true); //true for external transfer
        } else if(action.equals("Logout")){
            System.out.println("Bye Bye!");
            System.exit(1);
//---------------------------------------------------------------------------------------------------------------------- UNKNOWN REQUEST
        } else {
            System.out.println("Unknown Request (\"" + action + "\")");
            Transaction();
        }
    }

    public static void Withdraw(){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                String account = "";
                String bank = "";
                if(lst.get(0).equals("Customer")){ //Gets the bank that the customer wants to withdraw from
                    while(true){
                        System.out.println("----------------------");
                        System.out.println("What bank would you like to withdraw from?");
                        bank = input.nextLine(); //Bank they want to withdraw from
                        if(banks.contains(bank) || bank.equals("Skip")){ //If the bank exists within their banks, it continues, else it tries again, Skip skips the action and goes back
                            break;
                        } else {
                            System.out.println("You do not own an account in this bank!");
                        }   
                    }
                } else {
                    bank = lst.get(9); //Sets the employees bank to the bank they work at
                }

                while(!bank.equals("Skip")){ //Skips if previous action was skipped
                    System.out.println("----------------------");
                    System.out.println("What account would you like to withdraw from?");
                    account = input.nextLine(); //Gets the acccount number they want to withdraw from
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    if((allAccnts.get(pair) != null) || account.equals("Skip")){
                        break; //Checks if the pair of account exists within their accesible account, or if they want to skip
                    } else {
                        System.out.println("Account is not yours!");
                    }   
                }
                
                while(!(account.equals("Skip") || bank.equals("Skip"))){ //Checks if previously skipped
                    System.out.println("----------------------");
                    System.out.println("How much $ would you like to withdraw?");
                    String money = input.nextLine(); //Withdraw ammount
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    if(Float.parseFloat(allAccnts.get(pair)) >= Float.parseFloat(money)){ //Checks if there is enough money in the account
                        Statement st4 = conn.createStatement(); //Updates balance in database
                        st4.executeUpdate("UPDATE account SET balance = balance - " + money + " WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                        ResultSet rtID = st4.executeQuery("SELECT count(*) FROM transaction"); //gets the amount of transactions and sets the
                        rtID.next(); String tID = rtID.getString(1); 
                        st4.executeUpdate("INSERT INTO transaction VALUES (" + tID + ", " + money + ", CURRENT_DATE, 'withdraw', " + account + ", null, '" + bank + "')");
                        ResultSet rs4 = st4.executeQuery("SELECT balance FROM account WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                        rs4.next(); 
                        String newBal = (rs4.getString(1)); //Gets new balance from database
                        allAccnts.put(pair,newBal); //Updates balance in dictionary
                        st4.close();
                        System.out.println("New balance is: $" + newBal);
                        System.out.println("----------------------");
                        Transaction();
                    } else {
                        System.out.println("Not enough money!");
                        Transaction();
                    }   
                }
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }
    
    public static void Deposit(){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                String account = "";
                String bank = "";
                if(lst.get(0).equals("Customer")){ //Gets the bank that the customer wants to withdraw from
                    while(true){
                        System.out.println("----------------------");
                        System.out.println("What bank would you like to deposit into?");
                        bank = input.nextLine(); //Bank they want to deposi into
                        if(banks.contains(bank) || bank.equals("Skip")){ //If the bank exists within their banks, it continues, else it tries again, Skip skips the action and goes back
                            break;
                        } else {
                            System.out.println("You do not own an account in this bank!");
                        }   
                    }
                } else {
                    bank = lst.get(9); //Sets the employees bank to the bank they work at
                }

                while(!bank.equals("Skip")){ //Skips if previous action was skipped
                    System.out.println("----------------------");
                    System.out.println("What account would you like to deposit into?");
                    account = input.nextLine(); //Gets the acccount number they want to deposit into
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    if((allAccnts.get(pair) != null) || account.equals("Skip")){
                        break; //Checks if the pair of account exists within their accesible account, or if they want to skip
                    } else {
                        System.out.println("Account is not yours!");
                    }   
                }
                
                while(!(account.equals("Skip") || bank.equals("Skip"))){ //Checks if previously skipped
                    System.out.println("----------------------");
                    System.out.println("How much $ would you like to deposit?");
                    String money = input.nextLine(); //Deposit ammount
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    Statement st4 = conn.createStatement(); //Updates balance in database
                    st4.executeUpdate("UPDATE account SET balance = balance + " + money + " WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                    ResultSet rsID = st4.executeQuery("SELECT count(*) FROM transaction"); //Selects total amount of transaction and sets the tID to that
                    rsID.next(); String sID = rsID.getString(1); 
                    st4.executeUpdate("INSERT INTO transaction VALUES (" + sID + ", " + money + ", CURRENT_DATE, 'deposit', " + account + ", null, '" + bank + "')");
                    ResultSet rs4 = st4.executeQuery("SELECT balance FROM account WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                    rs4.next(); 
                    String newBal = (rs4.getString(1)); //Gets new balance from database
                    allAccnts.put(pair,newBal); //Updates balance in dictionary
                    st4.close();
                    System.out.println("New balance is: $" + newBal);
                    System.out.println("----------------------");
                    Transaction();
                }  
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }
    
    public static void Transfer(Boolean bool){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {
                String account = "";
                String bank = "";
                String accountTo = "";
                String bankTo = "";
                if(lst.get(0).equals("Customer")){ //Gets the bank that the customer wants to transfer from
                    while(true){
                        System.out.println("----------------------");
                        System.out.println("What bank would you like to transfer from?");
                        bank = input.nextLine(); //Bank they want to transfer from
                        if(banks.contains(bank) || bank.equals("Skip")){ //If the bank exists within their banks, it continues, else it tries again, Skip skips the action and goes back
                            break;
                        } else {
                            System.out.println("You do not own an account in this bank!");
                        }   
                    }
                } else {
                    bank = lst.get(9); //Sets the employees bank to the bank they work at
                }

                Dictionary<List<String>, String> bankAccnts = new Hashtable<List<String>, String>(); //Dictionary of key [aid, bank] and value balance
                Statement st5 = conn.createStatement(); // GETS ROWS FROM EMPLOYEE TABLE INTO rs2
                ResultSet rs5 = st5.executeQuery("SELECT * FROM account WHERE bank = '" + bank + "'"); // arr[5] and [6] are Username and Password
                int length5 = rs5.getMetaData().getColumnCount();
                while(rs5.next()){
                    String temp1 = "";
                    List<String> pair = new ArrayList<String>(); //Pair of Account ID and Bank 
                    for (int i = 1; i <= length5; i++){
                        if(i == 1){
                            pair.add(rs5.getString(i)); //adds account IDs to the pair
                        }
                        if(i == 2){
                            temp1 = rs5.getString(i); //gets the balance of account
                        }
                        if(i == 5){
                            pair.add(rs5.getString(i)); //adds the bank to the pair
                            bankAccnts.put(pair, temp1); //puts the pairs of aIDs and banks into the dictionary as a key with balance as the value
                        }
                    }
                }
                st5.close();
                while(!bank.equals("Skip")){ //Skips if previous action was skipped
                    System.out.println("----------------------");
                    System.out.println("What account would you like to transfer from?");
                    account = input.nextLine(); //Gets the acccount number they want to transfer from
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    if((allAccnts.get(pair) != null) || account.equals("Skip")){
                        break; //Checks if the pair of account exists within their accesible account, or if they want to skip
                    } else {
                        System.out.println("Account is not yours!");
                    }   
                }
                List<String> allBanks = new ArrayList<String>(); //list of customers banks 
                Statement st6 = conn.createStatement(); // GETS ROWS FROM EMPLOYEE TABLE INTO rs2
                ResultSet rs6 = st6.executeQuery("SELECT DISTINCT bank FROM branch"); // arr[5] and [6] are Username and Password
                int length6 = rs6.getMetaData().getColumnCount();
                while(rs6.next()){ 
                    for (int i = 1; i <= length6; i++){
                        if(i == 1){
                            allBanks.add(rs6.getString(i)); //adds the bank to the pair
                        }
                    }
                }

                if(bool){
                    while(!(account.equals("Skip") || bank.equals("Skip"))){
                        System.out.println("----------------------");
                        System.out.println("What bank would you like to transfer to?");
                        bankTo = input.nextLine(); //Bank they want to transfer to
                        if(allBanks.contains(bankTo) || bankTo.equals("Skip")){ //If the bank exists, it continues, else it tries again, Skip skips the action and goes back
                            break;
                        } else {
                            System.out.println("This bank does not exist!");
                        }   
                    }
                }else{
                    bankTo = bank;
                }

                while(!(account.equals("Skip") || bank.equals("Skip") || bankTo.equals("Skip"))){ //Skips if previous action was skipped
                    System.out.println("----------------------");
                    System.out.println("What account would you like to transfer to?");
                    accountTo = input.nextLine(); //Gets the acccount number they want to transfer to
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(accountTo); pair.add(bank); //Makes a list of [account, bank]                   
                    if((bankAccnts.get(pair) != null) || accountTo.equals("Skip")){
                        break; //Checks if the pair of account exists within the bank, or if they want to skip
                    } else {
                        System.out.println("Account doesnt exist in this bank!");
                    }   
                }

                while(!(account.equals("Skip") || bank.equals("Skip") || bankTo.equals("Skip") || accountTo.equals("Skip"))){ //Checks if previously skipped
                    System.out.println("----------------------");
                    System.out.println("How much $ would you like to transfer?");
                    String money = input.nextLine(); //Withdraw ammount
                    List<String> pair = new ArrayList<String>(); 
                    pair.add(account); pair.add(bank); //Makes a list of [account, bank]
                    List<String> pair2 = new ArrayList<String>(); 
                    pair2.add(accountTo); pair2.add(bankTo); //Makes a list of [account, bank]
                    if(Float.parseFloat(allAccnts.get(pair)) >= Float.parseFloat(money)){ //Checks if there is enough money in the account
                        Statement st4 = conn.createStatement(); //Updates balance in database
                        st4.executeUpdate("UPDATE account SET balance = balance - " + money + " WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                        st4.executeUpdate("UPDATE account SET balance = balance + " + money + " WHERE accountID = " + accountTo + " AND bank = '" + bankTo + "'"); 
                        ResultSet rtID = st4.executeQuery("SELECT count(*) FROM transaction"); //gets the amount of transactions and sets the
                        rtID.next(); String tID = rtID.getString(1); 
                        st4.executeUpdate("INSERT INTO transaction VALUES (" + tID + ", " + money + ", CURRENT_DATE, 'external transfer', " + account + ", " + accountTo + ", '" + bank + "')");
                        ResultSet rs4 = st4.executeQuery("SELECT balance FROM account WHERE accountID = " + account + " AND bank = '" + bank + "'"); 
                        rs4.next(); 
                        String newBal = (rs4.getString(1)); //Gets new balance from database
                        ResultSet rs42 = st4.executeQuery("SELECT balance FROM account WHERE accountID = " + accountTo + " AND bank = '" + bankTo + "'"); 
                        rs42.next(); 
                        String newBal2 = (rs42.getString(1)); //Gets new balance from database
                        allAccnts.put(pair,newBal); //Updates balance in dictionary
                        if((allAccnts.get(pair2) != null)){
                            allAccnts.put(pair2,newBal2); //Updates balance in dictionary
                        }
                        st4.close();
                        Transaction();
                    } else {
                        System.out.println("Not enough money!");
                        Transaction();
                    }   
                };
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }

    /*
    public static void Empty(){
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:9400/postgres", "postgres", "Landa123")){ //Connects to databse with port 9400, username postgres and Password Landa123
            if (conn != null) {

            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());} catch (Exception e) {e.printStackTrace();}
    }
    */
}