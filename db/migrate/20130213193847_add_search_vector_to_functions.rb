class AddSearchVectorToFunctions < ActiveRecord::Migration
  def self.up
    # 1. Create the search vector column
    add_column :functions, :search_vector, 'tsvector'

    # 2. Create the gin index on the search vector
    execute <<-SQL
      CREATE INDEX functions_search_idx
      ON functions
      USING gin(search_vector);
    SQL

    # 4 (optional). Trigger to update the vector column 
    # when the functions table is updated
    execute <<-SQL
      DROP TRIGGER IF EXISTS functions_search_vector_refresh
      ON functions;
      CREATE TRIGGER functions_search_vector_refresh
      BEFORE INSERT OR UPDATE
      ON functions
      FOR EACH ROW EXECUTE PROCEDURE
      tsvector_update_trigger (search_vector, 'pg_catalog.english', name, doc);
    SQL

    Function.find_each { |p| p.touch }
  end

  def self.down
    remove_column :functions, :search_vector
    execute <<-SQL
      DROP TRIGGER IF EXISTS functions_search_vector_refresh on functions;
    SQL
  end
end
