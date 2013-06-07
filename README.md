# Overtone Docs - a respectful fork of the ClojureDocs Web App

I wanted to contribute to the Overtone project by adding some
documentation. The standard doc generators for Clojure didn't really cut
it (no search for example) so I decided to fork this project instead. I
happen to think it's one of the best documentation sites available,
regardless of language or library, so I knew it would be a good fit.

There are a few Overtone specific changes but they're mostly cosmetic.
The big change is changing from MySQL to Postgres and removing the Thinking Sphinx gem. This version of the site uses 
Postgres full-text search instead. It's hosted on Heroku and you can see
it in action at [http://overtone-docs.herokuapp.com](http://overtone-docs.herokuapp.com).

# ClojureDocs Web App

[ClojureDocs](http://clojuredocs.org) is a community powered documentation and examples repository designed to aid clojurists of all skill levels in groking clojure core and third party libraries.

The alpha version of ClojureDocs was released on July 9th, 2010.  See the [original mailing list post](http://groups.google.com/group/clojure/browse_thread/thread/a97d472679f2cade/810b73543fd6a2a5?q=clojuredocs&lnk=ol&) for more information.

ClojureDocs consists of three main projects: this website, the [library importer](https://github.com/zkim/clojuredocs-analyzer), and the [external API](http://github.com/dakrone/cd-wsapi.git).

## Requirements
* Ruby 1.8.7 & Rails 2.1.5.
* Postgres
* bundler
* RVM - not required, but highly suggested.

## Getting Started
* Open up a terminal.
* Clone the repo: `git clone https://github.com/xavriley/clojuredocs.git`
* `cd clojuredocs`
* update the config/database.yml file with your details
    - alternatively, set the environment variables for your database user:
    - `export DBUSER=mypguser`
    - `export DBPASS=mypgpass`
* Install required gems: `bundle install`
* Create the required databases: `rake db:create`
* Run database migrations: `rake db:migrate`
* Start the dev server: `script/server`
* Navigate to `http://localhost:3000`

## License
ClojureDocs is licensed under the EPL v1.0 http://opensource.org/licenses/eclipse-1.0.php
