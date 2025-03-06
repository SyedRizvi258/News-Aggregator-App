import React, { useState, useEffect } from "react";
import "../Navbar.css";


/*
 * Navbar Component
 * 
 * A responsive navigation bar that displays a list of category links.
 * When a category is selected, it notifies the parent component.
 */
const Navbar = ({ onCategorySelect, resetCategory }) => {
  
  // List of available categories to be displayed in the navbar
  const categories = [
    "Tech", "Sports", "Business", "Politics", "Health", 
    "Entertainment", "Science", "Lifestyle", "Finance", "Education", "Environment"
  ];

  // State for controlling mobile menu open/close
  const [isOpen, setIsOpen] = useState(false);

  // State for tracking which category is currently selected
  const [activeCategory, setActiveCategory] = useState(null);

  // Function to handle category selection
  const handleCategorySelect = (category) => {
    setActiveCategory(category); // Update local state with selected category
    onCategorySelect(category); // Notify parent component about selection
  };

  // Reset active category when resetCategory changes
  useEffect(() => {
    if (resetCategory) {
      setActiveCategory(null);
    }
  }, [resetCategory]);

  return (
    <nav className="navbar navbar-expand-lg">
      <div className="container">
        <a className="navbar-brand" href="/home">QuickByte</a>
        
        {/* Mobile Menu Button */}
        <button 
          className="navbar-toggler" 
          onClick={() => setIsOpen(!isOpen)}
        >
          ☰
        </button>

        {/* Navbar Links */}
        <div className={`collapse navbar-collapse ${isOpen ? "show" : ""}`}>
          <ul className="navbar-nav ms-auto">
            {/* Create a button for each category */}
            {categories.map((category) => (
              <li key={category} className="nav-item">
                <button 
                  onClick={() => handleCategorySelect(category)} // Handle category select
                  className={activeCategory === category ? "active" : ""} // Highlight active category
                >
                  {category}
                </button>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
