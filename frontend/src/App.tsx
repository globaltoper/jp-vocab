import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { Navbar } from "./components/Navbar";
import { Footer } from "./components/Footer";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { WordCardPage } from "./pages/WordCardPage";
import { WordDetailPage } from "./pages/WordDetailPage";
import { SavedWordsPage } from "./pages/SavedWordsPage";
import { ReviewPage } from "./pages/ReviewPage";
import { DictationPage } from "./pages/DictationPage";
import { TypingPracticePage } from "./pages/TypingPracticePage";
import { LoginPage } from "./pages/LoginPage";
import { SignupPage } from "./pages/SignupPage";
import { FindUsernamePage } from "./pages/FindUsernamePage";
import { ResetPasswordPage } from "./pages/ResetPasswordPage";
import { VerifyEmailPage } from "./pages/VerifyEmailPage";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main className="app-main">
          <Routes>
            <Route path="/" element={<WordCardPage />} />
            <Route path="/words/:wordId" element={<WordDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/find-username" element={<FindUsernamePage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/verify-email" element={<VerifyEmailPage />} />
            <Route path="/dictation" element={<DictationPage />} />
            <Route path="/typing-practice" element={<TypingPracticePage />} />
            <Route
              path="/saved"
              element={
                <ProtectedRoute>
                  <SavedWordsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/review"
              element={
                <ProtectedRoute>
                  <ReviewPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </main>
        <Footer />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
