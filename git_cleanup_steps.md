# Git Cleanup Steps to Fix API Key Issues

GitHub detected an OpenAI API key in your repository history, which is blocking your pushes. Follow these steps to fix the issue:

## Option 1: Use GitHub's Allow Button (Easiest but Less Secure)

1. Visit the link provided by GitHub in the error message:
   ```
   https://github.com/avelon1A/prepStack/security/secret-scanning/unblock-secret/39AuypNxABexT9klg8UJQMizR0U
   ```

2. Click "Allow" to inform GitHub that this is not a real secret or that you've already revoked the key.

3. Push your changes again:
   ```bash
   git push origin main
   ```

## Option 2: Clean History (More Secure, Recommended)

This approach removes the API key from your Git history:

1. Ensure you've saved any important changes

2. Change the API key on your OpenAI account
   - Go to https://platform.openai.com/api-keys
   - Revoke the exposed key
   - Create a new key for future use

3. Create a new branch from the current main:
   ```bash
   git checkout main
   git checkout -b cleaned-history
   ```

4. Remove the API key from current files:
   - Edit `SETUP_API_KEY.md`, `local.properties.example`, and any other files with the key
   - Replace actual keys with placeholder text like `YOUR_ACTUAL_KEY_HERE`
   - Commit these changes:
     ```bash
     git add SETUP_API_KEY.md local.properties.example
     git commit -m "fix: replace API key with placeholder"
     ```

5. Use git filter-repo to clean history (first install it):
   ```bash
   # On macOS with Homebrew:
   brew install git-filter-repo
   
   # Then run:
   git filter-repo --invert-paths --path local.properties --path SETUP_API_KEY.md --path local.properties.example
   ```

6. Add back the clean files:
   ```bash
   git add SETUP_API_KEY.md local.properties.example
   git commit -m "chore: add setup files with placeholders"
   ```

7. Ensure local.properties is properly ignored:
   ```bash
   git rm --cached local.properties
   echo "local.properties" >> .gitignore
   git add .gitignore
   git commit -m "chore: ensure local.properties is ignored"
   ```

8. Force push to GitHub:
   ```bash
   git push -f origin cleaned-history
   ```

9. On GitHub, create a Pull Request from `cleaned-history` to `main`
   - Review the changes
   - Merge the PR (this will replace main with your cleaned history)

10. Update your local main:
    ```bash
    git checkout main
    git pull origin main
    ```

## After Fixing

1. Make sure your local.properties file is in .gitignore
2. Store API keys only in local.properties (never in tracked files)
3. Consider using GitHub Actions secrets for CI/CD pipelines

## For Team Members

Have your team members:
1. Clone the new repository after you've fixed it
2. Create their own local.properties file using local.properties.example as a template
3. Add their own API key to their local file