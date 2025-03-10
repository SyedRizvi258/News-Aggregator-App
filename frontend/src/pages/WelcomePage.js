import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../WelcomePage.css';
import Navbar from "../components/Navbar";


/*
* WelcomePage
*
* The main page of the application that displays news articles after login.
* It allows users to search for articles, view top headlines, and view articles by category.
* It also allows users to view and manage their favorite articles.
*/
const WelcomePage = () => {
  // Navigation and auth
  const { logout, auth } = useAuth(); // Get logout function and auth state from context
  const navigate = useNavigate(); // For navigating after logout

  // Mode and category selection
  const [mode, setMode] = useState("headlines"); // modes: "headlines", "search", "category", "favorites"
  const [selectedCategory, setSelectedCategory] = useState(""); // Selected category
  const [resetCategory, setResetCategory] = useState(false); // Reset category selection

  // Article data
  const [articles, setArticles] = useState([]); // State to store news articles
  const [favoriteArticles, setFavoriteArticles] = useState(new Set()); // Set of favorite article IDs

  // Search
  const [searchQuery, setSearchQuery] = useState(''); // State for search input
  const [submittedQuery, setSubmittedQuery] = useState(''); // State for submitted search query

  // Pagination
  const [currentPage, setCurrentPage] = useState(1); // Current page number
  const [pageSize] = useState(12); // Number of articles per page

  // UI states
  const [loading, setLoading] = useState(true); // State for loading spinner
  const [loadingFavorites, setLoadingFavorites] = useState(false); // Loading state for favorites
  const [error, setError] = useState(null); // State for error handling


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

      // Update articles state with top headlines
      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);


  // Fetch articles based on search query
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

      // Update articles state with search results
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

      // Update articles state with chosen category articles
      setArticles(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);


  // Fetch user's favorite articles
  const fetchFavoriteArticles = useCallback(async () => {
    if (!auth.isLoggedIn) return; // Don't fetch if user is not logged in
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
      
      // If in "favorites" mode, then set these as the articles to display
      if (mode === "favorites") {
        setArticles(data);
        setLoading(false);
      }
      
      // Create a set of favorite article IDs for efficient lookup
      const favoriteIds = new Set(data.map(article => article.id));
      setFavoriteArticles(favoriteIds);
    } catch (error) {
      console.error("Error fetching favorites:", error);
    } finally {
      setLoadingFavorites(false);
    }
  }, [auth.isLoggedIn, auth.userId, mode]);


  // Display user's favorite articles
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
      
      // If user has no favorites, set articles to empty array
      if (response.status === 204) {
        // No content
        setArticles([]);
        return;
      }
      
      if (!response.ok) {
        throw new Error('Failed to fetch favorites');
      }
      
      const data = await response.json();
      setArticles(data); // Set articles to user's favorite articles
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
    
    // Check if article is already in favorites
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
        const newFavorites = new Set(favoriteArticles); // Create a new set
        newFavorites.delete(article.id); // Remove the article ID
        setFavoriteArticles(newFavorites); // Update the state
        
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
        const newFavorites = new Set(favoriteArticles); // Create a new set
        newFavorites.add(article.id); // Add the article ID
        setFavoriteArticles(newFavorites); // Update the state
      }
    } catch (error) {
      console.error("Error toggling favorite:", error);
      alert("There was an error updating your favorites");
    }
  };


  // Function to handle different modes
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

  
  // Load user's favorites on login
  useEffect(() => {
    if (auth.isLoggedIn) {
      fetchFavoriteArticles();
    }
  }, [auth.isLoggedIn, fetchFavoriteArticles]);


  // Handle category selection
  const handleCategorySelect = (category) => {
    setSearchQuery(category); // Set search query to category
    setCurrentPage(1);
    setMode("category");
    setSelectedCategory(category);
    setResetCategory(false);
  };


  // Handle search query submission
  const handleFormSubmit = (e) => {
    e.preventDefault();
    if (searchQuery) {
      setSelectedCategory(""); // Reset selected category if any
      setCurrentPage(1); 
      setMode("search");
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
        {/* Page Title */}
        <h3><b>
          {mode === "favorites" ? "My Favorite Articles" : 
           mode === "search" ? `Search Results: "${submittedQuery}"` :
           mode === "category" ? `${selectedCategory} News` :
           "Top Headlines"}
        </b></h3>

        {/* Username Display, My Favorites Button, and Logout Button */}
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
