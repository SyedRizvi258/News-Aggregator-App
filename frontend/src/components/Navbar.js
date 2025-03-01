import React, { useState, useEffect } from "react";
import "../Navbar.css";

const Navbar = ({ onCategorySelect, resetCategory }) => {
  const categories = [
    "Tech", "Sports", "Business", "Politics", "Health", 
    "Entertainment", "Science", "Lifestyle", "Finance", "Education", "Environment"
  ];

  const [isOpen, setIsOpen] = useState(false);
  const [activeCategory, setActiveCategory] = useState(null);

  const handleCategorySelect = (category) => {
    setActiveCategory(category); // Set the active category
    onCategorySelect(category); // Call the passed function to notify the parent
  };

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

        <div className={`collapse navbar-collapse ${isOpen ? "show" : ""}`}>
          <ul className="navbar-nav ms-auto">
            {categories.map((category) => (
              <li key={category} className="nav-item">
                <button 
                  onClick={() => handleCategorySelect(category)} // Update active category on click
                  className={activeCategory === category ? "active" : ""} // Add 'active' class for the selected category
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