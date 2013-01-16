class Category < ActiveRecord::Base
  has_and_belongs_to_many :functions

  named_scope :top_level, :conditions => "name NOT LIKE '%>%'", :order => "name ASC"
  named_scope :sub_categories_scope, lambda { |parent_cat| 
    {
        :conditions => ["name LIKE '?' OR name LIKE '?%'", parent_cat[:name], parent_cat[:name]],
        :order => "name ASC"
    }
  }

  def self.top_level_category_names
    Category.find_by_sql("SELECT DISTINCT ON (cat_name) name,
                          POSITION(' ' IN name),
                            SUBSTRING(name FROM 1 FOR POSITION(' ' IN name)) AS cat_name
                            FROM categories
                            WHERE name LIKE '% %'").map(&:cat_name)
  end

  def sub_categories
    Category.sub_categories_scope(self.name)
  end
end
