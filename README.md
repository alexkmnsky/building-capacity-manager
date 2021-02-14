# Building Capacity Manager
Created in collaboration with [Maxwell Hunt](https://github.com/Maxwell-Hunt). 

## Inspiration
Maxwell, one of our teammates, has a part time job at David’s NoFrills in Mississauga where he has constantly kept track of the number of people in the store.   Despite his best efforts, it is not uncommon for Max to miss people going into and out of the store from time to time since he’s also performing other duties such as cleaning shopping carts and helping customers.  This isn’t good for the customers at NoFrills since when there are too many people in a confined space, the risk of contracting Covid-19 or other illnesses can go up exponentially.  It also isn’t good for NoFrills since if they are caught with too many people in the building, they can be fined a very heavy price.  We thought of the Building Capacity Manager as the perfect solution to this problem.

## What it does
The building capacity manager uses a basic electronic set up combined with a mobile app to allow business owners to quickly and easily check how many people are in their store or if they have reached the maximum number of people allowed into their store due to Covid-19 regulations.  

## How we built it
The large stores that typically have problems keeping track of the number of customers they have at any given time also tend to have two doors, one for the entrance and one for the exit.  Our idea was to set up a laser tripwire attached to a Raspberry Pi computer over both of these doors to count the number of people that pass in and out of the store.  This information is then broadcast to our mobile app (made with android studio) using TCP connections & sockets.

## Challenges we ran into and what we learned
Before starting this project both of us had very limited knowledge of electronics and how to set them up with the Raspberry Pi.  We also didn’t know how to use android studio, which is what we ended up using to create our mobile app.  Learning both of these technologies required much frustrating experimentation throughout the weekend.  However, by far our biggest challenge was using Sockets.  Going into the hackathon we were somewhat familiar with socket programming in python, however we had no clue when it came to doing the client side programming in Java for our mobile app.  Problems related to sockets were definitely the problems we spent most of our energy solving.

## What's next for the Building Capacity Manager
For the demo we were unable to demonstrate the simultaneous detection of people moving into the building and out of the building due to a lack of time to create the proper physical setup.  However, we have a solid idea of how we would approach this in the future.  Furthermore, we’d like to add functionality related to sending notifications to business owners when the number of people in the building surpasses the specified capacity.

## Screenshots

Description                  | Screenshot
-----------------------------|-------------------------------------------------------
Demo                         | ![Demo](https://i.ibb.co/Wf9Rz8V/0.png)
Of course we have dark mode! | ![Dark Mode](https://i.ibb.co/dc2vdfr/2.png)
Changing maximum capacity    | ![Maximum Capacity](https://i.ibb.co/5rw7wgg/3.png)
Settings page                | ![Settings](https://i.ibb.co/G302kxT/4.png)
Reconnection attempt         | ![Disconnected](https://i.ibb.co/fpXwk6f/5.png)
