import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../WelcomePage.css';
import Navbar from "../components/Navbar";

const WelcomePage = () => {
  const { logout, auth } = useAuth(); // Get logout function and auth state from context
  const navigate = useNavigate(); // For navigating after logout
  const [articles, setArticles] = useState([]); // State to store news articles
  const [searchQuery, setSearchQuery] = useState(''); // State for search input
  const [loading, setLoading] = useState(true); // State for loading spinner
  const [error, setError] = useState(null); // State for error handling
  const [currentPage, setCurrentPage] = useState(1); // Current page number
  const [pageSize] = useState(12); // Number of articles per page
  const [submittedQuery, setSubmittedQuery] = useState('');
  const [mode, setMode] = useState("headlines"); // "headlines" or "search" or "category" or "favorites"
  const [selectedCategory, setSelectedCategory] = useState(""); // Selected category
  const [resetCategory, setResetCategory] = useState(false); // Reset category selection
  const [favoriteArticles, setFavoriteArticles] = useState(new Set()); // Set of favorite article IDs
  const [loadingFavorites, setLoadingFavorites] = useState(false); // Loading state for favorites

  // Fetch top headlines
  const fetchTopHeadlines = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${process.env.REACT_APP_BASE_URL}/api/news/top-headlines?country=us&page=${currentPage}&pageSize=${pageSize}`
      );
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Failed to fetch top headlines');
      }

      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  // Fetch articles based on search query and pagination
  const handleSearch = useCallback(async () => {
    if (!submittedQuery.trim()) return; // Avoid making an empty request
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${process.env.REACT_APP_BASE_URL}/api/news/search?query=${submittedQuery}&page=${currentPage}&pageSize=${pageSize}`
      );
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Failed to fetch search results');
      }

      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [submittedQuery, currentPage, pageSize]);

  // Fetch articles based on category
  const fetchCategoryArticles = useCallback(async (category) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.REACT_APP_BASE_URL}/api/news/search?query=${category}&page=${currentPage}&pageSize=${pageSize}`
      );
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || "Failed to fetch category articles");
      }

      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  // Fetch user's favorite articles
  const fetchFavoriteArticles = useCallback(async () => {
    if (!auth.isLoggedIn) return;
    
    setLoadingFavorites(true);
    try {
      const response = await fetch(
        `${process.env.REACT_APP_BASE_URL}/api/favorites/${auth.userId}`
      );
      
      if (response.status === 204) {
        // No content (user has no favorites)
        setFavoriteArticles(new Set());
        return;
      }
      
      if (!response.ok) {
        throw new Error('Failed to fetch favorites');
      }
      
      const data = await response.json();
      
      // If we're in "favorites" mode, set these as the articles to display
      if (mode === "favorites") {
        setArticles(data);
        setLoading(false);
      }
      
      // Create a Set of favorite article IDs for efficient lookup
      const favoriteIds = new Set(data.map(article => article.id));
      setFavoriteArticles(favoriteIds);
    } catch (error) {
      console.error("Error fetching favorites:", error);
      // Don't set the main error state to avoid disrupting the main content view
    } finally {
      setLoadingFavorites(false);
    }
  }, [auth.isLoggedIn, auth.userId, mode]);

  // Display only favorite articles
  const showFavorites = useCallback(async () => {
    if (!auth.isLoggedIn) {
      setError("Please log in to view favorites");
      return;
    }
    
    setMode("favorites");
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        `${process.env.REACT_APP_BASE_URL}/api/favorites/${auth.userId}`
      );
      
      if (response.status === 204) {
        // No content
        setArticles([]);
        return;
      }
      
      if (!response.ok) {
        throw new Error('Failed to fetch favorites');
      }
      
      const data = await response.json();
      console.log("Favorites data:", data); // Add this log to see what's coming back
      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [auth.isLoggedIn, auth.userId]);

  // Add/remove article from favorites
  const toggleFavorite = async (article) => {
    if (!auth.isLoggedIn) {
      alert("Please log in to add favorites");
      return;
    }
    
    const isFavorite = favoriteArticles.has(article.id);
    
    try {
      if (isFavorite) {
        // Remove from favorites
        const response = await fetch(
          `${process.env.REACT_APP_BASE_URL}/api/favorites/${auth.userId}/remove/${article.id}`,
          {
            method: 'DELETE',
            credentials: 'include'
          }
        );
        
        if (!response.ok) {
          throw new Error('Failed to remove from favorites');
        }
        
        // Update local state
        const newFavorites = new Set(favoriteArticles);
        newFavorites.delete(article.id);
        setFavoriteArticles(newFavorites);
        
        // If in favorites mode, remove this article from the display
        if (mode === "favorites") {
          setArticles(articles.filter(a => a.id !== article.id));
        }
      } else {
        // Add to favorites
        const response = await fetch(
          `${process.env.REACT_APP_BASE_URL}/api/favorites/${auth.userId}/add/${article.id}`,
          {
            method: 'POST',
            credentials: 'include'
          }
        );
        
        if (!response.ok) {
          throw new Error('Failed to add to favorites');
        }
        
        // Update local state
        const newFavorites = new Set(favoriteArticles);
        newFavorites.add(article.id);
        setFavoriteArticles(newFavorites);
      }
    } catch (error) {
      console.error("Error toggling favorite:", error);
      alert("There was an error updating your favorites");
    }
  };

  useEffect(() => {
    if (mode === "search") {
      handleSearch();
    } else if (mode === "category") {
      fetchCategoryArticles(selectedCategory);
    } else if (mode === "favorites") {
      showFavorites();
    } else {
      fetchTopHeadlines();
    }
  }, [mode, submittedQuery, selectedCategory, currentPage, handleSearch, fetchTopHeadlines, fetchCategoryArticles, showFavorites]);

  // Load user's favorites when component mounts or auth changes
  useEffect(() => {
    if (auth.isLoggedIn) {
      fetchFavoriteArticles();
    }
  }, [auth.isLoggedIn, fetchFavoriteArticles]);

  // Handle category selection
  const handleCategorySelect = (category) => {
    setSearchQuery(category);
    setCurrentPage(1);
    setMode("category");
    setSelectedCategory(category);
    setResetCategory(false);
  };

  // Handle form submission for search
  const handleFormSubmit = (e) => {
    e.preventDefault();
    if (searchQuery) {
      setSelectedCategory(""); // Reset selected category
      setCurrentPage(1); // Reset to page 1 when searching
      setMode("search"); // Set mode to search
      setResetCategory(true);
      setTimeout(() => {
          setSubmittedQuery(searchQuery);
      }, 0);
    }
  };

  // Logout user and redirect to login page
  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <div className="container my-4">
      {/* Navbar */}
      <Navbar onCategorySelect={handleCategorySelect} resetCategory={resetCategory} />

      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3><b>
          {mode === "favorites" ? "My Favorite Articles" : 
           mode === "search" ? `Search Results: "${submittedQuery}"` :
           mode === "category" ? `${selectedCategory} News` :
           "Top Headlines"}
        </b></h3>
        {auth.isLoggedIn && (
          <div className="d-flex flex-column align-items-end">
            <span className="username">{auth.username}</span>
            <div>
              <button onClick={showFavorites} className="btn btn-primary mb-2">
                My Favorites
              </button>
            </div>
            <div>
              <button onClick={handleLogout} className="btn btn-danger">
                Logout
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Search Bar */}
      <form onSubmit={handleFormSubmit} className="mb-4">
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            placeholder="Search news articles..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Searching...' : 'Search'}
          </button>
        </div>
      </form>

      {/* Loading & Error Messages */}
      {loading && <div className="no-articles text-center">
        <i className="fas fa-newspaper fa-3x text-muted"></i>
        <p className="mt-3 text-muted fs-5">Loading...</p>
      </div>}
      {error && <div className="text-danger text-center">{error}</div>}
      {!loading && articles.length === 0 && (
        <div className="no-articles text-center">
          <i className="fas fa-newspaper fa-3x text-muted"></i>
          <p className="mt-3 text-muted fs-5">
            {mode === "favorites" ? "You don't have any favorite articles yet." : "No articles found."}
          </p>
        </div>
      )}

      {/* News Articles */}
      <div className="row">
        {articles.map((article, index) => (
          <div key={index} className="col-md-4 mb-4">
            <div className="card h-100">
              {article.imageUrl && (
                <img
                  src={article.imageUrl}
                  alt={article.title}
                  className="card-img-top"
                  style={{height: '200px', objectFit: 'cover'}}
                  onError={(e) => e.target.style.display = 'none'}
                />
              )}
              <div className="card-body">
                <h5 className="card-title">{article.title}</h5>
                <p className="card-text">{article.description}</p>
                <a
                  href={article.url}
                  className="btn btn-primary"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Read More
                </a>
              </div>
              <div className="card-footer d-flex justify-content-between align-items-center">
                <small className="text-muted">
                  Source: {article.sourceName} |{' '}
                  {new Date(article.publishedAt).toLocaleDateString()}
                </small>
                {auth.isLoggedIn && (
                  <button 
                    className="btn heart-btn" 
                    onClick={() => toggleFavorite(article)} 
                    disabled={loadingFavorites}
                    style={{
                      fontSize: "20px",
                      color: "gray",
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                    }}
                  >
                    {favoriteArticles.has(article.id) ? "Unlike ❤️" : "Like 🤍"}
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination Controls - Hide in favorites mode */}
      {!loading && articles.length > 0 && mode !== "favorites" && (
        <div className="pagination">
          <button
            onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
            className="btn btn-secondary"
            disabled={currentPage === 1}
          >
            Previous
          </button>
          <span className="mx-2">Page {currentPage}</span>
          <button
            onClick={() => {
              if (mode === "search") {
                setSubmittedQuery(submittedQuery); // Ensure search continues
              }
              setCurrentPage((prev) => prev + 1);
            }}
            className="btn btn-secondary ms-2"
            disabled={articles.length < pageSize}
          >
            Next
          </button>
        </div>
      )}
    </div> 
  );
};

export default WelcomePage;