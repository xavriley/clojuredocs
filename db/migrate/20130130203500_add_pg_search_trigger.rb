class AddPgSearchTrigger < ActiveRecord::Migration
  def self.up
    execute %q{
      CREATE TRIGGER functions_search_vector_refresh 
        BEFORE INSERT OR UPDATE ON functions 
        FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(search_vector, 'pg_catalog.english',  doc, name);
    }
  end

  def self.down
  end
end
