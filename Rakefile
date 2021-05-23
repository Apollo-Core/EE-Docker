# frozen_string_literal: true

task :build do
  desc 'build Docker image'
  sh 'docker', 'build', '.', '-t', 'ee-docker'
end

task :run => [:build] do
  desc 'run Docker image'
  sh 'docker', 'run', '-p', '5055:5055', 'ee-docker'
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
