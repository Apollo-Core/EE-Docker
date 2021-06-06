# frozen_string_literal: true

task :build do
  desc 'build Docker image'
  sh 'docker', 'build', '.', '-t', 'ee-docker'
end

task :scan_container, [:name] do |name: 'ee-docker'|
  desc 'check a running Docker container'
  sh 'docker', 'run', '--network', 'ee-docker-network', '-it', 'instrumentisto/nmap', name
end

task :network do
  desc 'create Docker network'
  sh 'docker', 'network', 'create', 'ee-docker-network' do |ok, res|
    # Ignore error if network already exists.
  end
end

task :run => [:build, :network] do
  desc 'run Docker image'
  sh 'docker', 'run', '--name', 'ee-docker', '--network', 'ee-docker-network', '-p', '5055:5055', 'ee-docker'
end

task :cleanup do
  desc 'clean local build artifacts and prune Docker images'
  sh './gradlew', 'clean'
  sh 'docker', 'container', 'prune'
end

task :dependencies do
  sh 'npm', 'install', '-g', 'artillery'
end

task :integration do
  Dir.chdir('integration-tests') do
    sh 'artillery', 'run', 'artillery.yml'
  end
end
