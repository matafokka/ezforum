# ezforum
Easy forum written in Java

# About
## In general
Ezforum is written in Java using these parts of Java EE stack:
1. CDI Beans
1. JPA (Hibernate, dunno about other implementations)
1. JSF

Ezforum has been tested on Wildfly 18 and PostgreSQL 11.1. It should work with other servers and RDBMS, but there's no official guarantee.

Key features of ezforum:
1. Lightweight frontend: the whole thing is made only with HTML and CSS. JavaScript is used only in two parts, and sripts are really small and fast. Additionaly, ezforum can work even without JS, though, BBCodes buttons won't work.
1. Themes support.
   1. Themes are located in `themes` directory inside deployment's root.
   1. All themes are based on "rainy" theme.
   1. Copy it, open, throw away unneeded stuff, replace stuff you want. You know, what to do.
1. Custom locales (translations) support.
   1. Locales are located in `WEB-INF/classes/locales` inside deployment's root.
   1. Locales are basically Java EE properties files but with UTF-8 support.
   1. Each official locale contains tutorial on translation.
1. Security.
   1. XSS, SQL Injections and bad URLs won't work here.
   1. All sensitive data is encryped, however, there are some pitfalls (see "AES Configuration").
   1. However, ezforum DOES NOT provide any protection against DDOS or spam attacks. Use firewall, DDOS mitigation software or CDNs.
1. Scalability that comes with Java EE servers.
1. Famous Java backwards compatibility: ezforum needs JRE 8 or higher.

## Structure of the forum
The structure of the forum is pretty generic:
1. Sections - groups multiple discussion themes in one category.
1. Subsections - abstract discussion themes.
1. Topics - actual discussions started by users.
1. Posts - replies from users to a specific topic.

## User roles (ranks)
Ezforum defines following roles (ranks):
0. Creator - creator of the forum, a person who has access to the server. Ideally, there should be only one creator, but you can give this rank to your close friends.
1. Super Administrator - maintains the forum content in general.
1. Administrator - meant to oversee specific topics.
1. Moderator - resolves users' problems.
1. User - a user of the forum.
1. Unregistered - not an actual role. Can read, but can't write.

Additionally, following rules are applied:
* Every user can complain about every user (except creators) and their topics and posts. Users, who's ranks are higher than both receiver's and sender's ranks, should resolve these complaints in administration panel.
* Only creators and super administrators can create and remove sections and subsections.
* Creators, super administrators and administrators can edit anyone's topics and posts in order to maintain them.
* Users and moderators can edit their topics and posts in 10 hours from the moment of publishing.
* Users with higher ranks can maintain accounts of users with lower ranks, including:
  * Changing their ranks.
  * Banning and unbanning them.
  * Removing their topics and posts.
  * Resolving complaints from or about them.
  
# Installation
This part describes general deployment process. For exact instructions, read the docs for your RDBMS and Java EE server.

## Database
1. Download and install your favourite RDBMS server. But, as being said above, it is recommended to use PostgreSQL.
1. Create a database and a schema:
   1. Open `ezforum.sql` script at project's root directory.
   1. Replace all occurences of database, schema and user names (`postgres`, `ezforum` and `user` respectively) with yours.
   1. Run it on your server.
  
## Java EE Server
1. Download and install your favourite Java EE server. But, as being said above, it is recommended to use WildFly.
1. If your server doesn't come with Hibernate, install it. Other JPA implementations are not officially supported.
1. Setup JTA pointing to ezforum schema.

## Ezforum
### Preparation
1. Download and install JDK (at least for Java 8).
1. Download and install maven.
1. Download and extract the source code.

### JPA Configuration
1. Open file `/src/main/java/META-INF/persistence.xml`
1. Find following text (should be on line 15): `<property name="currentSchema" value="ezforum"/>`
1. Change `ezforum` to your schema name.

### Email Configuration
You need to setup an Email server, so ezforum can send confirmation messages when user registers or changes email.
1. Install your favourite email server. It should support SMTP protocol.
1. Open file `/src/main/java/ez/forum/util/Email/config.cfg` and follow instructions in it.

### AES Configuration
Ezforum encrypts sensitive data using AES 256. You need to enter your master key.
1. Open file `/src/main/java/ez/forum/util/AES/AESMasterKey.txt` and follow instructions in it.
1. Again, write your key somewhere else and keep it in secret! Don't use default key!

### Deployment
I recommend use Exploded WAR packaging, so it will be easy to install custom locales and themes.
1. `cd` to the project's root.
1. Run `mvn compile war:exploded`.
1. Deploy compiled project and run it.
1. Create an account for creator:
   1. Register a new user using ezforum.
   1. Change it's rank to 0 in the database.
