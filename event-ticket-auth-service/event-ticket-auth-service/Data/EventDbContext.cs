using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace event_ticket_auth_service.Data
{
    public class EventDbContext : IdentityDbContext<IdentityUser>
    {
        public EventDbContext(DbContextOptions<EventDbContext> options) : base(options)
        {
        }
    }
}
