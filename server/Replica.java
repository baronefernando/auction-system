import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.List;

public class Replica implements ReplicaInterface{
  
  List<AuctionItem> items = new ArrayList<AuctionItem>();
  List<String> users = new ArrayList<String>();
  List<CurrentAuctions> auctions = new ArrayList<CurrentAuctions>();

  List<Integer> ReplicaPool = new ArrayList<Integer>();
  boolean isPrimaryReplica = false;

  public Replica()
  {
    super();
    //Will check if there is a primary replica available and syncronise its data with it.
    try 
    {
      Registry registry = LocateRegistry.getRegistry("localhost");
      Auction FrontEnd = (Auction) registry.lookup("FrontEnd");
      int primaryID = FrontEnd.getPrimaryReplicaID();

      ReplicaInterface primaryReplica = (ReplicaInterface) registry.lookup("Auction" + primaryID);
      updateReplicaAuctions(primaryReplica.getReplicaAuctions());
      updateReplicaItems(primaryReplica.getReplicaItems());
      updateReplicaUsers(primaryReplica.getReplicaUsers());
      updateReplicaPool(primaryReplica.getReplicaPool());

      System.out.println("This Replica has syncronised with the primary replica the following:\nItems: " + items.size() + "\nUsers: " + users.size() + "\nAuctions: " + auctions.size());
    } 
    catch (Exception e) 
    {
      System.out.println("No primary replica found.");
    }
  }

  //Returns an auction item
  public AuctionItem getSpec(int itemID) 
  {
    if(items.size() > 0)
    {
      for (int i = 0; i < items.size(); i++) 
      {
        if(items.get(i).itemID == itemID)
        {
          updateOtherReplicas();
          return (AuctionItem) items.get(i);
        }
      }
    }

    return null;    
  }
  //Adds a new user
  public NewUserInfo newUser(String email)
  {
    NewUserInfo newUser = new NewUserInfo();
    newUser.privateKey = null;
    newUser.publicKey = null;

    if(users.size() > 0)
    {
      for (int i = 0; i < users.size(); i++) 
      {
        if(users.get(i).contains(email))
        {
          newUser.userID = i;
          return newUser;
        }
      }
    }
    users.add(email);
    newUser.userID = users.size()-1;
    updateOtherReplicas();
    return newUser;
  }

  //Creates a new auction
  public synchronized int newAuction(int userID, AuctionSaleItem item)
  {
    items.add(new AuctionItem());
    
    items.get(items.size()-1).itemID = (items.size()-1);
    items.get(items.size()-1).name = item.name;
    items.get(items.size()-1).description = item.description;
    items.get(items.size()-1).highestBid = item.reservePrice;

    auctions.add(new CurrentAuctions(userID, (items.size()-1), userID)); //add to current auctions
    updateOtherReplicas();
    return 0;
  }

  //Lists all auction items
  public AuctionItem[] listItems()
  {
    AuctionItem[] myAuctionItems = new AuctionItem[items.size()];
    myAuctionItems = items.toArray(myAuctionItems);
    updateOtherReplicas();
    return myAuctionItems;
  }

  //Closes an auction and works out the winner
  public AuctionCloseInfo closeAuction(int userID, int itemID)
  {
    for (int i = 0; i < auctions.size(); i++) 
    {
      if(auctions.get(i).itemID == itemID)
      {
        if(auctions.get(i).userID == userID)
        {
          AuctionCloseInfo winner = new AuctionCloseInfo();
          winner.winningEmail = users.get(auctions.get(i).highestBidUserID);

          for (int j = 0; j < items.size(); j++) 
          {
            if(items.get(j).itemID == itemID)
            {
              winner.winningPrice = items.get(j).highestBid;
            }
          }
          
          items.remove(i);
          auctions.remove(i);
          updateOtherReplicas();
          return winner;
        }
      }
    }

    AuctionCloseInfo invalidUser = new AuctionCloseInfo();
    invalidUser.winningEmail = "invalid";
    invalidUser.winningPrice = 0;
    return invalidUser;
  }

  //Bids in an auction
  public synchronized boolean bid(int userID, int itemID, int price)
  {
    for (int i = 0; i < items.size(); i++) 
    {
      if(items.get(i).itemID == itemID)
      {
        if(items.get(i).highestBid >= price)
        {
          return false;
        }
        else if(items.get(i).highestBid < price)
        {
          items.get(i).highestBid = price;

          for(int j = 0; j < auctions.size(); j++)
          {
            if(auctions.get(j).itemID == itemID)
            {
              auctions.get(j).highestBidUserID = userID;
            }
          }

          updateOtherReplicas();
          return true;
        }
      }
    }
    return false;
  }

  //This method will update all non-primary replicas datasets.
  private void updateOtherReplicas()
  {
    try 
    {
      Registry registry = LocateRegistry.getRegistry("localhost");
      for (int i = 0; i < ReplicaPool.size(); i++) 
      {
        ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + ReplicaPool.get(i));
        if(server.getPrimaryStatus() == false)
        {
          server.updateReplicaPool(ReplicaPool);
          server.updateReplicaItems(items);
          server.updateReplicaAuctions(auctions);
          server.updateReplicaUsers(users);
        }
      }
      System.out.println("Replicas updated.");
    } 
    catch (Exception e) 
    {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }

  //Replica setters
  public void updatePrimaryStatus(boolean primaryStatus)
  {
    isPrimaryReplica = primaryStatus;
  }
  public void updateReplicaPool(List<Integer> allReplicas)
  {
    ReplicaPool = allReplicas;
  }
  public void updateReplicaUsers(List<String> newUsers)
  {
    users = newUsers;
  }
  public void updateReplicaItems(List<AuctionItem> newItems)
  {
    items = newItems;
  }
  public void updateReplicaAuctions(List<CurrentAuctions> newAuctions)
  {
    auctions = newAuctions;
  }

  //Replica getters
  public boolean getPrimaryStatus()
  {
    return isPrimaryReplica;
  }
  public List<Integer> getReplicaPool()
  {
    return ReplicaPool;
  }
  public List<String> getReplicaUsers()
  {
    return users;
  }
  public List<AuctionItem> getReplicaItems()
  {
    return items;
  }
  public List<CurrentAuctions> getReplicaAuctions()
  {
    return auctions;
  }
  
  //Auction interface not used methods
  public int getPrimaryReplicaID(){return 0;}
  public byte[] challenge(int userID){return null;}
  public boolean authenticate(int userID, byte signature[]){return false;}
  
  public static void main(String[] args) 
  {
    try 
    {
      Replica s = new Replica();
      String name = "Auction" + args[0];
      ReplicaInterface stub = (ReplicaInterface) UnicastRemoteObject.exportObject(s, 0);
      Registry registry = LocateRegistry.getRegistry();
      registry.rebind(name, stub);

      System.out.println("Replica is running...");
      System.out.println("\nPress [Ctrl] + [C] to exit.");
    } 
    catch (Exception e) 
    {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }  
}