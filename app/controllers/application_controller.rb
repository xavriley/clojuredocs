# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  protect_from_forgery # See ActionController::RequestForgeryProtection for details

  filter_parameter_logging :password, :password_confirmation
  helper_method :current_user_session, :current_user
  before_filter :default_library

  private
  def default_library
    @library = Library.find(:first, :conditions => {:current => true})
  end
  
  def json_fail(message)
    return {:json => {:success => false, :message => message}}
  end
  
  def current_user_session
    return @current_user_session if defined?(@current_user_session)
    @current_user_session = UserSession.find
  end

  def current_user
    return @current_user if defined?(@current_user)
    @current_user = current_user_session && current_user_session.user
  end

  def redirect_back_or_default(path)
    redirect_to :back
    rescue ActionController::RedirectBackError
    redirect_to path
  end
  
  def find_recently_updated(size, lib = nil)
    recent = []
    
    comments = []
    examples = []
    see_alsos = []
    
    if not lib
      comments = Comment.find(:all, :limit => size, :order => 'updated_at DESC')
      examples = Example.find_by_sql(["select * from example_versions order by updated_at DESC limit ?", size])
#      raise examples.to_yaml
      see_alsos = SeeAlso.find(:all, :limit => size, :order => 'updated_at DESC')
    else
      
       comments = Comment.find(:all, 
        :joins => "INNER JOIN functions ON comments.commentable_id = functions.id LEFT JOIN namespaces ON functions.namespace_id = namespaces.id LEFT JOIN libraries ON namespaces.library_id = libraries.id",
        :conditions => ["libraries.name = ?", lib],
        :limit => size, 
        :order => 'updated_at DESC')
        
       examples = Example.find(:all, 
          :include => [:function, {:function => :namespace}, {:function => {:namespace => :library}}],
          :conditions => {:functions => {:namespaces => {:libraries => {:name => lib}}}},
          :limit => size, 
          :order => 'examples.updated_at DESC')
      
       see_alsos = SeeAlso.find(:all, 
          :include => [:from_function, {:from_function => :namespace}, {:from_function => {:namespace => :library}}],
          :conditions => {:from_functions => {:namespaces => {:libraries => {:name => lib}}}},
          :limit => size, 
          :order => 'see_alsos.updated_at DESC')  
    end
    
    recent = (comments + examples + see_alsos).sort{|a,b| b.updated_at <=> a.updated_at}
      
    if recent.size > size
      recent = recent[0, size]
    end
    
    recent
  end
  
  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password
end
