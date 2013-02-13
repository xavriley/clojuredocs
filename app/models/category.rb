class Category < ActiveRecord::Base
  has_and_belongs_to_many :functions

  named_scope :top_level, :conditions => "name NOT LIKE '%>%'", :order => "name ASC"
  named_scope :sub_categories_scope, lambda { |parent_cat_name| 
    {
        :conditions => ["name LIKE ? OR name LIKE ?", parent_cat_name, "#{parent_cat_name}%"],
        :order => "name ASC"
    }
  }

  def self.top_level_categories
    cat_names = Category.find_by_sql("SELECT DISTINCT ON (cat_name) name,
                          POSITION(' ' IN name),
                            SUBSTRING(name FROM 1 FOR POSITION(' ' IN name)) AS cat_name
                            FROM categories
                            WHERE name LIKE '% %'")
    cat_names.map do |ct|
      {:name => ct.cat_name,
       :groups => find_subgroups(ct.cat_name)}
    end
  end

  def self.find_subgroups(top_level_category_name)
    Category.sub_categories_scope(top_level_category_name).map do |group|
      { 
        :name => group.name,
        :symbols => find_symbols(group)
      }
    end
  end

  def self.find_symbols(group)
    Function.find(:all, 
                  :conditions => [
                    "categories_functions.category_id = ? 
                    AND functions.name NOT LIKE ? 
                    AND functions.name NOT LIKE ?", group.id,"%:kr", "%:ar"], 
                    :joins => :categories).map {|fnc|
      {
        :name => fnc.name,
        :ns => fnc.namespace.name,
        :link => "/v/#{fnc.id}",
        :id => fnc.id
      }
    }
  end
end
