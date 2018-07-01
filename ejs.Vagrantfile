Vagrant.configure("2") do |config|

  #https://www.vagrantup.com/docs/networking/public_network.html
  #First available in this list gets selected:
  #TODO: Linux, etc.
  config.vm.network "public_network", auto_config: false, bridge: [
    "bridge0",
    "en1: Thunderbolt 1"
  ]


  <% AUTOGEN_CIDRS.forEach(function(cidr,i){ %>

  config.vm.define "hg<%= i %>" do |hg<%= i %>|
    hg<%= i %>.vm.box = "ubuntu/bionic64"
    hg<%= i %>.vm.network :forwarded_port, guest: <%= Number(PORT_BASE_INT)+Number(i) %>, host: <%= Number(PORT_BASE_EXT)+Number(i) %>
    #hg<%= i %>.vm.provision :shell, path: "bootstrapVM.sh"
    hg<%= i %>.vm.provision "shell" do |s|
      s.path = "bootstrapVM.sh"
      #address, netmask, network, broadcast
      s.args   = ["<%= cidr %>","<%= AUTOGEN_NETMASK %>","<%= AUTOGEN_SUBNET %>","<%= AUTOGEN_BROADCAST %>"]
    end
  end

  <% }) %>

end
