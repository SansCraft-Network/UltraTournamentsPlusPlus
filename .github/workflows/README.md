# GitHub Actions Workflows

This directory contains automated workflows for the UltraTournamentsPlusPlus project.

## Workflows

### 🔨 Build and Test (`build.yml`)
**Triggers**: Push to `main`/`develop`, Pull Requests to `main`

**What it does**:
- Tests the plugin on Java 17 and 21
- Runs Maven tests
- Builds the plugin JAR
- Caches Maven dependencies for faster builds
- Uploads build artifacts and test results

### 🚀 Release (`release.yml`)
**Triggers**: When a tag starting with `v` is pushed (e.g., `v1.0.0`)

**What it does**:
- Builds a release version of the plugin
- Creates a GitHub release with changelog
- Uploads the plugin JAR as a release asset
- Automatically updates version numbers

### ✅ Pull Request Check (`pr-check.yml`)
**Triggers**: When PRs are opened or updated

**What it does**:
- Validates the Maven project structure
- Compiles and tests the code
- Posts build status comments on PRs
- Ensures code quality before merging

## Usage

### For Development
1. **Push code** to `main` or `develop` → Automatic build and test
2. **Create PR** → Automatic validation and status updates
3. **Merge PR** → Triggers full build pipeline

### For Releases
1. **Create and push a tag**:
   ```bash
   git tag v1.2.3
   git push origin v1.2.3
   ```
2. **Automatic release** will be created with the built JAR file
3. **Download** the JAR from the GitHub Releases page

## Requirements

- Repository must have `GITHUB_TOKEN` (automatically provided)
- Java 17+ for building
- Maven project structure
- Spigot/Paper dependencies configured

## Artifacts

- **Build artifacts**: Available for 30 days after each build
- **Test results**: Available for 7 days after each build
- **Release assets**: Permanent download links for each version

## Status Badges

Add these to your README.md:

```markdown
![Build Status](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/workflows/Build%20and%20Test/badge.svg)
![Release](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/workflows/Release/badge.svg)
```