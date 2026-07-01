import React, { useState, useRef, useEffect } from "react";
import "./ChatBot.css";

function ChatBot() {
  const [messages, setMessages] = useState([
    {
      id: 1,
      text: "🐾 Hello! I'm PawCare Assistant. Ask me anything about animal care, first aid, or emergencies!",
      sender: "bot",
      timestamp: new Date()
    }
  ]);
  const [inputMessage, setInputMessage] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const sendMessage = async () => {
    if (!inputMessage.trim()) return;

    // Add user message
    const userMessage = {
      id: messages.length + 1,
      text: inputMessage,
      sender: "user",
      timestamp: new Date()
    };
    setMessages(prev => [...prev, userMessage]);
    setInputMessage("");
    setIsTyping(true);

    try {
      const response = await fetch("http://localhost:8080/api/chatbot/message", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ message: inputMessage })
      });

      const data = await response.json();
      
      // Add bot response
      const botMessage = {
        id: messages.length + 2,
        text: data.reply,
        sender: "bot",
        timestamp: new Date(),
        category: data.category
      };
      setMessages(prev => [...prev, botMessage]);
    } catch (error) {
      console.error("Error:", error);
      const errorMessage = {
        id: messages.length + 2,
        text: "Sorry, I'm having trouble connecting. Please try again later. 🐾",
        sender: "bot",
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      sendMessage();
    }
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const quickQuestions = [
    "Emergency first aid",
    "My dog ate chocolate",
    "How to care for a stray",
    "Vaccination schedule",
    "Heat stroke prevention"
  ];

  return (
    <>
      {/* Chatbot Button */}
      {!isOpen && (
        <button className="chatbot-button" onClick={() => setIsOpen(true)}>
          💬
          <span className="chatbot-badge">🐾</span>
        </button>
      )}

      {/* Chatbot Window */}
      {isOpen && (
        <div className="chatbot-container">
          <div className="chatbot-header">
            <div className="chatbot-header-info">
              <span className="chatbot-avatar">🐾</span>
              <div>
                <h3>PawCare Assistant</h3>
                <p>Online • Ready to help animals</p>
              </div>
            </div>
            <button className="chatbot-close" onClick={() => setIsOpen(false)}>
              ✕
            </button>
          </div>

          <div className="chatbot-messages">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`message ${message.sender === "user" ? "user-message" : "bot-message"}`}
              >
                <div className="message-content">
                  {message.sender === "bot" && <span className="bot-icon">🐾</span>}
                  <div className="message-text">
                    {message.text.split('\n').map((line, i) => (
                      <React.Fragment key={i}>
                        {line}
                        {i < message.text.split('\n').length - 1 && <br />}
                      </React.Fragment>
                    ))}
                  </div>
                  <div className="message-time">{formatTime(message.timestamp)}</div>
                </div>
              </div>
            ))}
            {isTyping && (
              <div className="message bot-message">
                <div className="message-content">
                  <span className="bot-icon">🐾</span>
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick Questions */}
          <div className="quick-questions">
            {quickQuestions.map((question, index) => (
              <button
                key={index}
                className="quick-question-btn"
                onClick={() => {
                  setInputMessage(question);
                  inputRef.current?.focus();
                }}
              >
                {question}
              </button>
            ))}
          </div>

          <div className="chatbot-input">
            <input
              ref={inputRef}
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Ask me about animal care, first aid, emergencies..."
              className="chatbot-input-field"
            />
            <button onClick={sendMessage} className="chatbot-send-btn">
              Send 📤
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export default ChatBot;