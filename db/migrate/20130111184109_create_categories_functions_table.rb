class CreateCategoriesFunctionsTable < ActiveRecord::Migration
  def self.up
    create_table :categories_functions do |t|
      t.integer :category_id
      t.integer :function_id
      t.index :category_id, :function_id, :name => 'category_id_idx'
    end
  end

  def self.down
    drop_table :categories_functions 
  end
end
