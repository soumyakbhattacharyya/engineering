## Git

# Part - 1 : Command References 

* **Start a working area**
   * clone (`Clone a repository into a new directory`) 
   * init (`Create an empty Git repository or reinitialize an existing one`)

* **Work on the current change**
   - add (`Add file contents to the index`)
   - mv (`Move or rename a file, a directory, or a symlink`)
   - reset (`Reset current HEAD to the specified state`)
   - rm (`Remove files from the working tree and from the index`)

- **examine the history and state**
    - bisect (`Use binary search to find the commit that introduced a bug`)
  - grep (`Print lines matching a pattern`)
  - log (`Show commit logs`)
  - show (`Show various types of objects`)
  - status (`Show the working tree status`)

- **grow, mark and tweak your common history**
    - branch (`List, create, or delete branches`)
  - checkout (`Switch branches or restore working tree files`)
  - commit (`Record changes to the repository`)
  - diff (`Show changes between commits, commit and working tree, etc`)
  - merge (`Join two or more development histories together`)
  - rebase (`Reapply commits on top of another base tip`)
  - tag (`Create, list, delete or verify a tag object signed with GPG`)

- **collaborate**
    - fetch (`Download objects and refs from another repository`) 
    - pull (`Fetch from and integrate with another repository or a local branch`)
    - push (`Update remote refs along with associated objects`)


 
# Part - 2 : Useful Aliases

Following content should be placed inside .gitconfig file of a git repository. Issue `git tell` to get an understanding about what is possible with this aliases.

  ```
    [core]
        symlinks = false
        repositoryformatversion = 0
        filemode = false
        logallrefupdates = true
    [remote "origin"]
        url = https://github.com/soumyakbhattacharyya/engineering.git
        fetch = +refs/heads/*:refs/remotes/origin/*
    [branch "master"]
        remote = origin
        merge = refs/heads/master
    [alias]
      # <<utility>>
      # checkout a to specific branch
      co = checkout -b
  
      # cm allows adding new file to the index and committing the same with subsequent message
      cm = !git add -A && git commit -m
      
      # pull is equivalent to svn update command, in additonal --rebase ensures that local commit history are written on top of what is pulled
      # --prune ensures removal of remote tracking branches that no longer exists on remote 
      up = !git pull --rebase --prune
      
      # log shows commit history graphically 
      lg = !git  log --graph --oneline --decorate --all
      
      # save a checkpoint
      save = !git add -A && git commit -m 'SAVEPOINT'
      
      # amend a commit
      amend = !git commit -a --amend
      
      # mark a useless development which has been reset
      wipe = !git add -A && git commit -qm 'WIPE SAVEPOINT' && git reset HEAD~1 --hard
      
      # merge changes that you did on the brnach to master
      fuse = !git merge        
  
      # push with tags 
      send = !git push --tags origin master
  
      # tag
      tg = !git tag -a
      
      # <tell>
      tell = !echo "git co new_branch to create a new branch" && echo "git cm give_commit_message" && echo "git save" && echo "git amend" && echo "git fuse new_branch to master" && echo "git tg tag_name to create a tag" && echo "git send to sync local master with remote" 
      
      # Concepts
  
      # HEAD : last commit and parent of the next commit
      # index : proposed set of new changes
      # working copy : local sandbox (which might have changes that require to be indexed)
  
      # Additional Command Reference
  
      # git init # turn this directory into a git directory 
      # git rm somefile # remove file from index and filesystem 
      # git rm --cached # remove file from index ONLY 
      # git add file1 file2 file3 # add updated files to the git index 
      # git status # get a brief summary about changes to the index, etc. 
      # git revert [commit number] # revert back to a particular version
  ``` 
   


# Part - 3 : Git - Flow

### Initialization

To initialize a new repo with the basic branch structure, use:
  
    git flow init [-d]
  
This will then interactively prompt you with some questions on which branches
you would like to use as development and production branches, and how you
would like your prefixes be named. You may simply press Return on any of
those questions to accept the (sane) default suggestions.

The ``-d`` flag will accept all defaults.


### Creating feature/release/hotfix/support branches

* To list/start/finish feature branches, use:
    ```    
      git flow feature
      git flow feature start <name> [<base>]
      git flow feature finish <name>
    ```
  For feature branches, the `<base>` arg must be a commit on `develop`.

* To push/pull a feature branch to the remote repository, use:
    ```
      git flow feature publish <name>
      git flow feature pull <remote> <name>
    ```
* To list/start/finish release branches, use:
  
    ```
      git flow release
      git flow release start <release> [<base>]
      git flow release finish <release>
    ```
    
  For release branches, the `<base>` arg must be a commit on `develop`.
  
* To list/start/finish hotfix branches, use:
   
    ```
      git flow hotfix
      git flow hotfix start <release> [<base>]
      git flow hotfix finish <release>
    ```
  For hotfix branches, the `<base>` arg must be a commit on `master`.

* To list/start support branches, use:
      
    ```
      git flow support
      git flow support start <release> <base>
    ```
  
  For support branches, the `<base>` arg must be a commit on `master`.


