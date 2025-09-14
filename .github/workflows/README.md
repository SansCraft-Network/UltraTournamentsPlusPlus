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
- **NEW**: Automatically creates development releases for main branch builds

### 🚧 Development Release (`dev-release.yml`) 
**Triggers**: Push to `main` branch, Manual workflow dispatch

**What it does**:
- **Automatically creates development releases** for every push to main
- Generates versioned build artifacts (e.g., `1.0.0-dev.123.abc1234`)
- Creates comprehensive release notes with recent changes
- Manages release retention (keeps only last 5 dev releases)
- Tags commits for future reference
- Can be skipped by adding `[skip-release]` to commit message

### 🚀 Stable Release (`release.yml`)
**Triggers**: When a tag starting with `v` is pushed (e.g., `v1.0.0`)

**What it does**:
- Builds a stable release version of the plugin
- Creates a GitHub release with changelog
- Uploads the plugin JAR as a release asset
- Updates version numbers automatically

### ✅ Pull Request Check (`pr-check.yml`)
**Triggers**: When PRs are opened or updated

**What it does**:
- Validates the Maven project structure
- Compiles and tests the code
- Posts build status comments on PRs
- Ensures code quality before merging

### 🔒 Security Scan (`security.yml`)
**Triggers**: Push to main, PRs, Weekly schedule

**What it does**:
- OWASP dependency vulnerability scanning
- GitHub CodeQL security analysis
- Automated security reports
- Regular scheduled security checks

## Usage

### 🤖 **Automatic Development Releases**
Every time you push to the `main` branch, a development release is **automatically created**:

```bash
git push origin main
# → Automatic dev release: v1.0.0-dev.123.abc1234
```

**Skip auto-release** by including `[skip-release]` in your commit:
```bash
git commit -m "Fix typo in README [skip-release]"
```

### 📦 **Manual Development Release**
Trigger a development release manually with custom version:

1. Go to **Actions** → **Development Release**
2. Click **Run workflow**
3. Optional: Add custom version suffix
4. **Release created** with custom naming

### 🏷️ **Stable Releases**
Create official releases using tags:

```bash
git tag v1.2.3
git push origin v1.2.3
# → Official release: v1.2.3
```

### 🔄 **Regular Development**
1. **Push code** → Auto-build and auto-release (if main branch)
2. **Create PR** → Validation and status updates  
3. **Merge PR** → New development release created

## Release Types

| Type | Trigger | Example Version | Purpose |
|------|---------|----------------|---------|
| **Development** | Push to `main` | `1.0.0-dev.123.abc1234` | Latest features, automated |
| **Manual Dev** | Workflow dispatch | `1.0.0-custom-name` | Testing specific features |
| **Stable** | Version tag | `v1.2.3` | Production-ready releases |

## Artifacts & Downloads

### 📥 **Where to Download**
- **Latest Dev Build**: [Releases page](../../releases) → Look for 🚧 dev builds
- **Stable Releases**: [Releases page](../../releases) → Look for 🚀 stable releases
- **Build Artifacts**: Actions tab → Individual workflow runs

### 🗂️ **Artifact Retention**
- **Development releases**: Last 5 releases kept (older ones auto-deleted)
- **Stable releases**: Permanent
- **Build artifacts**: 30 days
- **Test results**: 7 days

## Features

### ✨ **Auto-Release Benefits**
1. **🔄 Continuous Delivery**: Every commit to main gets a release
2. **📋 Rich Release Notes**: Automatic changelog generation with recent commits
3. **🏷️ Smart Versioning**: Build numbers, commit hashes, timestamps
4. **🧹 Automatic Cleanup**: Old dev releases are cleaned up automatically
5. **⚡ Instant Availability**: No manual work required
6. **🛡️ Quality Gates**: Only successful builds create releases

### 🎯 **Smart Features**
- **Automatic version incrementing** based on build numbers
- **Commit history** in release notes since last release
- **Build metadata** including file sizes and test results
- **Cross-reference links** to commits, workflows, and comparisons
- **Installation instructions** in every release

## Requirements

- Repository must have `GITHUB_TOKEN` (automatically provided)
- Java 17+ for building
- Maven project structure
- Write access to create releases and tags

## Status Badges

Add these to your README.md:

```markdown
![Build Status](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/workflows/Build%20and%20Test/badge.svg)
![Dev Release](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/workflows/Development%20Release/badge.svg)
![Stable Release](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/workflows/Release/badge.svg)
```