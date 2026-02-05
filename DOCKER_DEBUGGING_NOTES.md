# 🔧 Docker Container Debugging Summary

## The Problem

The OpENer Docker image was successfully **building** but **failing to start** the container with error:
```
Error: stat /opener/build/bin/opener: no such file or directory
```

This was a classic Docker image mismatch issue where the build completed but the binary wasn't where expected.

---

## The Investigation Process

### Step 1: Identified Wrong Path
**Initial Dockerfile CMD:**
```dockerfile
CMD ["/opener/build/bin/opener"]
```
But the build logs didn't show where the binary actually went.

### Step 2: Shelled Into Running Image
```bash
docker run --rm opener-plc:latest ls -la /opener/build/
```
**Result**: Directory exists but no `bin/` subdirectory

### Step 3: Found the Actual Binary
```bash
docker run --rm opener-plc find /opener/build/src -type f -executable
# Output: /opener/build/src/ports/POSIX/OpENer
```
**Discovery**: Binary is at `/opener/build/src/ports/POSIX/OpENer`, not `/opener/build/bin/opener`

### Step 4: Binary Needs Arguments
```bash
docker run --rm opener-plc /opener/build/src/ports/POSIX/OpENer
# Output: Usage: /opener/build/src/ports/POSIX/OpENer [interface name]
```
**Discovery**: Binary requires network interface as argument

---

## The Fix

### Updated Dockerfile.build
```dockerfile
# OLD (line 31)
CMD ["/opener/build/bin/opener"]

# NEW (line 31)
CMD ["/opener/build/src/ports/POSIX/OpENer", "eth0"]
```

### Why This Works
- **Path**: `/opener/build/src/ports/POSIX/OpENer` is where OpENer's POSIX build system puts the executable
- **Interface**: `eth0` is the default network interface inside Docker containers
- **Container**: Docker forwards host ports to container eth0 (44818/tcp → eth0:44818)

---

## Before & After

### Before
```
❌ docker compose up -d
   Error: failed to create task for container
   Error: exec: "/opener/build/bin/opener": 
   stat /opener/build/bin/opener: no such file or directory
```

### After
```
✅ docker compose up -d
   [+] Running 1/1
   ✓ Container opener-plc  Started
   
✅ docker ps
   STATUS: Up 18 seconds (healthy)
   PORTS: 0.0.0.0:44818→44818/tcp, 0.0.0.0:2222→2222/udp
```

---

## Key Learnings

1. **Multi-stage Docker builds**: Binary location depends on build system (CMake in this case)
2. **Investigation tools**: `docker exec`, `docker run --rm`, `find` are your friends
3. **Container networking**: Default interface is usually `eth0` for bridge networks
4. **Build output vs runtime**: Just because build succeeds doesn't mean artifact is in expected location

---

## How to Avoid This in Future Projects

### Checklist
- [ ] Understand the build system's output directory structure
- [ ] Read build logs carefully (look for "Building..." messages)
- [ ] Test binary location before creating Dockerfile CMD
- [ ] Use `docker build --progress=plain` to see full build output
- [ ] Always shell into image after build to verify artifacts exist
- [ ] Document binary location in build notes

### Testing Pattern
```bash
# 1. Test in container after build
docker run --rm <image> ls -la /expected/path

# 2. Verify executable
docker run --rm <image> /full/path/to/binary --version

# 3. Check network requirements
docker run --rm <image> /full/path/to/binary
# See if it prints usage or requires arguments
```

---

## Files Modified

| File | Change | Result |
|------|--------|--------|
| `Dockerfile.build` | CMD path + eth0 arg | ✅ Container starts |
| `docker-compose.yml` | Removed obsolete version | ✅ No warnings |

---

## Timeline

| Time | Action | Result |
|------|--------|--------|
| Initial | Build Dockerfile with CMake | ✅ Image builds (no errors) |
| Issue | Container won't start | ❌ Binary not found |
| Debug | Shell into image, found binary | ✅ Located at actual path |
| Fix | Updated CMD path + argument | ✅ Container starts healthy |
| Verify | `docker ps`, `docker logs` | ✅ Running and healthy |

---

## Verification

```bash
# Verify container is healthy
docker ps --filter "name=opener-plc"
# STATUS should show: "Up XX seconds (healthy)"

# Verify binary is running
docker exec opener-plc ps aux | grep OpENer
# Should show the OpENer process

# Verify ports are listening
docker exec opener-plc netstat -tlnp | grep 44818
# Should show listening on port 44818
```

---

## Related Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| "Address already in use" | Port 44818 in use | `docker compose down` or change port mapping |
| Build fails with CMake error | Wrong platform variable | Use `OpENer_PLATFORM=POSIX` (exact case) |
| Docker daemon not running | Docker Desktop stopped | Start Docker Desktop |
| Container exits immediately | Binary path wrong | Shell into image to find real path |

---

## Documentation References

- [OpENer Build System](https://github.com/EIPStackGroup/OpENer)
- [Docker CMD Reference](https://docs.docker.com/engine/reference/builder/#cmd)
- [Linux find Command](https://linux.die.net/man/1/find)
- [Docker exec Documentation](https://docs.docker.com/engine/reference/commandline/exec/)

---

**This fix ensures the OpENer PLC simulator container can start successfully and communicate via EtherNet/IP on ports 44818/tcp and 2222/udp.**
