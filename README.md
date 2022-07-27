# Locavore

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Locavore helps consumers become "locavores" by finding farms and agritourism events within their designated radius.  

### Demo:
[![Locavore Demo](http://img.youtube.com/vi/YMuW5qZYyV8/0.jpg)](http://www.youtube.com/watch?v=YMuW5qZYyV8 "Locavore")
    
### App Evaluation
- **Category:** Social
- **Mobile:** To me, this app would work best on mobile rather than web because of the value of push notifications and the many different audiences who can be reached. 
- **Story:** allow people to find local farms and have on farm experiences. 
- **Market:** There are many available markets: the definition of agritourism events is very broad, and can be anything from horseback riding lessons to wine tastings at vineyards to corn mazes to sunflower festivals to pick-your-own produce to concerts/performances. These events all attract a wide variety of people, from young couples to families to the elderly to gen z and millenials. Farms of all shapes and sizes create their own unique charm, and have the capability of capturing the attention of a diverse array of audiences. This is an untapped market because farms mostly operate on word of mouth and thus unless well acquainted with an area, it can be hard for consumers to even know that agritourism events like these are taking place.
- **Habit:** Users can always find new farms and events near them. They could also receive push notifications for events from liked farms, upcoming events they're attending, etc, to keep them engaged. Additionally, events could be added to their calendars.
- **Scope:** All communities can benefit from an app like this: city folk can find events in the nearby countryside, and those who own the farms and live in those communities will benefit from the tourism and spending.

## Product Spec

### Satisfying Project Requirements:

**Your app provides multiple opportunities for you to overcome difficult/ambiguous technical problems:**
* sorting algorithm for events.
* optimization of database & network requests.

**Your app interacts with a database (e.g. Parse)**
* Parse database

**Your app integrates with at least one SDK (e.g. Google Maps SDK, Facebook SDK) or API (that you didn’t learn about in CodePath) – free SDKs and APIs only**
* Google Maps
* Yelp

**You can log in/log out of your app as a user**
* Using Parse database

**You can sign up with a new user profile**
* Using Parse database

**Your app has multiple views**
* Many views.

**Your app uses at least one gesture (e.g. double tap to like, e.g. pinch to scale)**
* User can double tap to like an event. 

**Your app incorporates at least one external library to add visual polish**
* Glide, Lottie

**Your app uses at least one animation (e.g. fade in/out, e.g. animating a view growing and shrinking)**
* within map screen, the recyclerview scrolls to the farm clicked on, and animates when that profile is collapsed.

### 1. User Stories (Required and Optional)

**Required Must-have Stories (Completed)**

* use parse database for:
     * signup / login for farmers (includes farmers markets)
     * signup / login for "locavores"
* home page including: 
    * farmer profile scroll view (horizontal recycler view) at the top
    * deals/sales/upcoming events from local farms (vertical recycler view)
    * user can click on event to see detail view on modal overlay
    * search bar where user can search local farms / filter tags
* map where locavores can see farms in their area (using the google maps API)
    * horizontal recycler view of farm profiles at the bottom
    * this scrolls to a farm and displays larger details view when a farm is clicked on the map
    * in the opposite direction, as the recycler view is scrolled through, zooms to different farms on the map
    * users can tap to be brought to the farm's more detailed profile
* farm profile
   * header fragment including profile picture, background picture, name, description, tags. 
   * header fragment collapses with cool scrolling animation as the user scrolls down
   * farmer can edit their profile information
   * horizontal recyclerview of events
   * horizontal recyclerview of reviews taken from Yelp API
   * farmers can create new events--> complete with photos, times, dates, description.
* user profile 
   * logout button
   * user can follow farms
   * user can mark events as attended, liked, disliked.

**Ideas for Future Features**
*  user can post reviews on farm profile
*  user can place an order to the farmer based on current crops in stock via an orders screen
*  user can view a "learn" tab in which various agriculture definitions can be found
*  chat feature where user can ask the farm questions
*  events that the user signs up for can be added to their google calendar 
*  user profile:
    *  profile photo, name, settings, vertical recycler view of events (past + upcoming) that I've attended / will be attending
    *  user can swipe left to archive an event
* push notifications with events from favorited(followed) farms (or just events that user "may like"?)

### 2. Screen Archetypes

* signup/login screen
   * two paths: locavore and farmer/farmers' market
* "feed" screen
   * home page stories
* map screen
    * map stories
* farm profile screen
* user profile screen

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Tab (home screen)
* Map tab (map screen)
* Orders Tab (orders screen)
* User Profile Tab (farmer profile screen OR user profile screen)

**Flow Navigation** (Screen to Screen)

* Farmer Signup / login
   * signup: set up profile
   * map screen
* Consumer Signup / login
   * signup: set up profile
   * map screen

## Wireframes
![](feed_screen.jpeg)
![](map_screen.jpeg)
![](farm_profile.jpeg)
![](event_creation.jpeg)
![](user_profile.jpeg)

### [BONUS] Digital Wireframes & Mockups
<img src="https://i.imgur.com/8JAKTOv.jpg" width=600 height>
<img src="https://i.imgur.com/T3XRRgC.png" width=600 height>


### [BONUS] Interactive Prototype

## Schema 

### Models
Farmer User
| Property        | Type                                | Description                             |
|-----------------|-------------------------------------|-----------------------------------------|
| email           | String                              | Email address associated with the farm  |
| password        | String                              | Password for logging in                 |
| farmName        | String                              | Farm Name                               |
| location        | String                              | Farm Address                            |
| profilePhoto    | File                                | Profile picture                         |
| profileBackdrop | File                                | Background photo for profile header     |
| bio             | String                              | Brief description / bio of farm         |
| events          | Array of pointers to Event objects  | Events that this farm has posted        |
| reviews         | Array of pointers to Review objects | Reviews that this farm has received     |
| tags            | Array of Strings                    | Tags that are associated with this farm |
| farmersMarket?  | Boolean                             | True/false if this is a farmers' market |

Locavore User

| Property                 | Type                                   | Description                                                                       |
|--------------------------|----------------------------------------|-----------------------------------------------------------------------------------|
| email                    | String                                 | user email                                                                        |
| password                 | String                                 | user account password                                                             |
| profilePhoto             | File                                   | user profile picture                                                              |
| name                     | String                                 | user's first/last name                                                            |
| farmsFavorited           | Array of pointers to Farm User Objects | farms that this user has favorited                                                |
| events                   | Array of pointers to Event Objects     | events that this user has attended/will attend                                    |
| pushNotificationsEnabled | Boolean                                | whether the user can receive push notifications or not                            |
| radius                   | Integer                                | Radius in miles in which user considers farms within driving distance (aka local) |

Event 

| Property | Type             | Description                     |
|----------|------------------|---------------------------------|
| name     | String           | Name of the event               |
| farm     | Pointer to User  | Name of the farm                |
| tags     | Array of Strings | Tags associated with this event |
| location | String           | Location of this event          |
|          |                  |                                 |
|          |                  |                                 |
|          |                  |                                 |
|          |                  |                                 |
|          |                  |                                 |
|          |                  |                                 |
|          |                  |                                 |

Review
| Property | Type | Description |
|----------|------|-------------|
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |
|          |      |             |


Example query:
    
    protected void queryEvents() {
        
    ParseQuery<Event> query = ParseQuery.getQuery(Event.class);    
    query.addDescendingOrder("distance");
        
    query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if (e != null)
                {
                    Log.e(TAG, "Issue with getting events", e);
                }
                else
                {
                    for (Event event : events)
                    {
                        Log.i(TAG, "Event: " + event.getDescription() + ", farm: " + post.getFarm().getName());
                    }
                    allEvents.addAll(events);
                    adapter.notifyDataSetChanged(); // modify to make more accurate
                }
            }
        });
    }
    
### Networking
## Requests by Screen
- Feed Screen
    - (Read/GET) Query all events within range
    - (Read/GET) Query all farmer profiles within range
- Profile Screen
    - (Read/GET) Query this user's information from Parse
    - (Read/GET) Query this user's Yelp reviews if farmer.
- Map Screen
    - Google Maps API: https://developers.google.com/maps/documentation/android-sdk/utility
    - (READ/GET) Query all farmer profiles within range

- Yelp API: https://knowledge.yelp.com/
- Google Maps API: https://developers.google.com/maps/documentation/android-sdk/utility
- Parse Database: https://www.back4app.com/
