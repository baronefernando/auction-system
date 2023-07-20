import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.ArrayList;

public class Client{
     public static void main(String[] args) 
     {
        try 
        {
            String name = "FrontEnd";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            
            int userID = 0;
            int userInput = 0;
            String userEmail = "";

            //User registration/log in
            while(true)
            {
                Scanner s = new Scanner(System.in);
                System.out.println("AUCTION SYSTEM");
                System.out.println("Created by: Fernando Barone\n");

                System.out.print("Enter your email address: ");
                userEmail = s.nextLine();

                if(userEmail.length() <= 0)
                {
                    System.out.println("\nPlease enter an email address!");
                }
                else
                {
                    System.out.println("\nWelcome, " + userEmail + "!");
                    System.out.print("Would you like to log in/register? Y/N: ");
                    String choice = s.nextLine();

                    if(choice.contains("y") || choice.contains("Y"))
                    {
                        NewUserInfo newUser = server.newUser(userEmail);
                        userID = newUser.userID;
                        break;
                    }
                }
            }

            //Main menu
            while(true)
            { 
                System.out.println("\nUSER ID: " + userID);
                System.out.println("Replica ID: " + server.getPrimaryReplicaID());
                System.out.println("Email: " + userEmail);
                
                System.out.println("\nOptions:");
                System.out.println("1-List Items");
                System.out.println("2-New Auction");
                System.out.println("3-Close Auction");
                System.out.println("4-Bid");
                System.out.println("5-Exit");

                System.out.print("\nChoose an option: ");
        
                try
                {
                    Scanner s = new Scanner(System.in);
                    userInput = s.nextInt();
                    s.nextLine();
                    System.out.println("");

                    if(userInput <= 0) //Zero or less
                    {
                        System.out.println("Available options: 1-4");
                    }
                    else if(userInput == 1) //List all items
                    {
                        AuctionItem[] showItems = server.listItems();
                        if(showItems.length == 0)
                        {
                            System.out.println("No items being auctioned. Please check again later...");
                        }
                        else
                        {
                            System.out.println("***********ITEMS AVAILABLE**********");
                        }
                        
                        for (int i = 0; i < showItems.length; i++)
                        {
                            System.out.println("************************************");
                            System.out.println("ITEM ID: " + showItems[i].itemID);
                            System.out.println("ITEM NAME: " + showItems[i].name);     
                            System.out.println("ITEM DESCRIPTION: " + showItems[i].description);
                            System.out.println("HIGHEST BID: £" + showItems[i].highestBid);
                            System.out.println("************************************");
                        }
                    }
                    else if(userInput == 2) //Create new Auction
                    {
                        AuctionSaleItem newItem = new AuctionSaleItem();
                        System.out.print("Enter item name: ");
                        newItem.name = s.nextLine();

                        System.out.print("Enter item description: ");
                        newItem.description = s.nextLine();

                        System.out.print("Enter reserve price: £");
                        newItem.reservePrice = s.nextInt();

                        server.newAuction(userID,newItem);
                        System.out.println("\nItem added successfully.");                        
                    }
                    else if(userInput == 3) //Close Auction
                    {
                        int itemLookup;
                        System.out.print("Enter item's itemID: ");
                        itemLookup = s.nextInt();
                        s.nextLine();

                        AuctionItem item = server.getSpec(itemLookup);
                        System.out.println("\n************************************");
                        System.out.println("ITEM ID: " + item.itemID);
                        System.out.println("ITEM NAME: " + item.name);     
                        System.out.println("ITEM DESCRIPTION: " + item.description);
                        System.out.println("HIGHEST BID: £" + item.highestBid);
                        System.out.println("************************************");

                        System.out.print("\nIs this the the auction you would like to close? Y/N: ");
                        String c = s.nextLine();

                        if(c.contains("y") || c.contains("Y"))
                        {
                            AuctionCloseInfo winner = server.closeAuction(userID, itemLookup);
                            if(winner.winningEmail.contains("invalid"))
                            {
                                System.out.println("\nOnly the user who created the auction can close it.");
                            }
                            else
                            {
                                System.out.println("\nAuction Closed!");
                                System.out.println("Sold to: " + winner.winningEmail);
                                System.out.println("Sold for: £" + winner.winningPrice);
                            }
                        }                   
                    }
                    else if(userInput == 4) //Bid
                    {
                        int itemLookup;
                        System.out.print("Enter item's itemID: ");
                        itemLookup = s.nextInt();
                        s.nextLine();

                        AuctionItem item = server.getSpec(itemLookup);
                        System.out.println("\n************************************");
                        System.out.println("ITEM ID: " + item.itemID);
                        System.out.println("ITEM NAME: " + item.name);     
                        System.out.println("ITEM DESCRIPTION: " + item.description);
                        System.out.println("HIGHEST BID: £" + item.highestBid);
                        System.out.println("************************************");

                        System.out.print("\nIs this the item you're looking for? Y/N: ");
                        String c = s.nextLine();

                        if(c.contains("y") || c.contains("Y"))
                        {
                            System.out.println("\nCurrent highest bid: £" + item.highestBid);
                            System.out.print("Enter your bid: £");
                            int userBid = s.nextInt();
                            s.nextLine();
                            boolean bid = server.bid(userID, itemLookup, userBid);

                            if(bid == true)
                            {
                                System.out.println("\nBid successfull");
                                item = server.getSpec(itemLookup);
                                System.out.println("Bid: £" + item.highestBid);
                            }
                            else
                            {
                                System.out.println("\nYou cannot bid the same current bid value or a lower value.");
                            }
                        }
                    }
                    else if(userInput == 5) //Exit
                    {
                        System.out.println("See you soon!");
                        break;
                    }
                }
                catch(Exception e)
                {
                    System.out.println("You entered an invalid option.");
                }
            }
        }
        catch (Exception e) 
        {
            System.err.println("Uh oh! No servers available. Try again later.");
        }
      }
}