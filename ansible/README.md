# Linux deployment with Ansible

This playbook prepares and deploys the portal on a Debian or Ubuntu host. It installs Docker Engine and the Compose v2 plugin from Docker's official APT repository when they are missing, copies the project build inputs, builds the configured image tag on the host, and starts the app with MySQL. Repeated runs update the same Compose project and retain its named database volume.

## Prerequisites

- Ansible Core 2.15 or newer on a Linux/WSL control node.
- SSH access and sudo privileges on an authorized Debian or Ubuntu x86_64, aarch64, or armv7l host.
- Internet access from the target for Docker's APT repository, base images, and Maven dependencies.
- No conflicting legacy Docker packages if Docker Engine is not already installed. The playbook stops instead of removing them.
- Docker Compose v2.20 or newer. The playbook installs the plugin when needed and verifies daemon access as root.

## Configure inventory

Copy `inventory/hosts.example.yml` to `inventory/hosts.yml`, then set the real authorized `ansible_host`, SSH user, deployment path, Compose project, application port/bind address, image tag, database name, and database user. The `.example.invalid` hostname is a placeholder and is not a real deployment address. The real inventory file is ignored by Git.

The example binds the web app to loopback. Change `app_bind_address` only if the host firewall and any reverse proxy are configured for the chosen exposure. MySQL is not published on a host port.

## Validate and run

From the repository root:

```sh
ansible-playbook --syntax-check -i ansible/inventory/hosts.example.yml ansible/deploy.yml
ansible-playbook --check --diff -i ansible/inventory/hosts.yml ansible/deploy.yml --ask-become-pass
ansible-playbook -i ansible/inventory/hosts.yml ansible/deploy.yml --ask-become-pass
```

The playbook prompts privately for the application and MySQL root passwords. They are passed to Compose through the process environment and are not written to the repository or image. Use strong values. Docker administrators can inspect container environment settings, so this approach is for a course deployment; use a dedicated secrets service for a hardened production host.

Check mode predicts supported package and file changes; Ansible does not execute the Compose deployment or the HTTP health request in check mode. A normal run waits for the Compose health checks and then requires HTTP 200 from the configured app port. The target build is local and does not publish an image to a registry.

The playbook has not been run against a real deployment host. Review the target inventory and network exposure before execution. It does not configure Windows or remove existing containers, images, or volumes.
